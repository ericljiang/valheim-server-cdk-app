package me.ericjiang.valheimservercdk.server.compute

import software.amazon.awscdk.services.ec2._
import software.constructs.Construct

class AutomatableCompute(scope: Construct, id: String) extends Construct(scope, id) {
  // create instance
  private val instance = Instance.Builder.create(this, "Instance")
    .instanceType(InstanceType.of(InstanceClass.BURSTABLE3_AMD, InstanceSize.MEDIUM))
    .machineImage(MachineImage.latestAmazonLinux)
    .vpc(Vpc.fromLookup(this, "DefaultVpc", VpcLookupOptions.builder.isDefault(true).build))
    .init(CloudFormationInit.fromElements(
      InitPackage.yum("docker"),
      InitFile.fromString("/etc/sysconfig/valheim-server",
        """SERVER_NAME=My Server
          |SERVER_PORT=2456
          |WORLD_NAME=Dedicated
          |SERVER_PASS=secret
          |SERVER_PUBLIC=true""".stripMargin),
      InitFile.fromUrl("/etc/systemd/system/valheim.service", "https://raw.githubusercontent.com/lloesche/valheim-server-docker/main/valheim.service"),
      // use this instead? https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-init.html#aws-resource-init-services
//      InitCommand.shellCommand(
//        """sudo systemctl daemon-reload
//          |sudo systemctl enable valheim.service
//          |sudo systemctl start valheim.service""".stripMargin)
    ))
    .build
  // allow traffic to Valheim server
  instance.getConnections.allowFromAnyIpv4(Port.udpRange(2456, 2458), "valheim")
  // update policy
}
