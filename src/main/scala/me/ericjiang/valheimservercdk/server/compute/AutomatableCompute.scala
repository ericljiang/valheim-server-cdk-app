package me.ericjiang.valheimservercdk.server.compute

import software.amazon.awscdk.services.ec2._
import software.constructs.Construct

class AutomatableCompute(scope: Construct, id: String) extends Construct(scope, id) {
  // create instance
  private val instance = Instance.Builder.create(this, "Instance")
    .instanceType(InstanceType.of(InstanceClass.BURSTABLE3_AMD, InstanceSize.MEDIUM))
    .machineImage(MachineImage.latestAmazonLinux(AmazonLinuxImageProps.builder
      .generation(AmazonLinuxGeneration.AMAZON_LINUX_2)
      .cachedInContext(true)
      .build))
    .vpc(Vpc.fromLookup(this, "DefaultVpc", VpcLookupOptions.builder.isDefault(true).build))
    .init(CloudFormationInit.fromElements(
      InitPackage.yum("docker"),
      InitFile.fromString("/etc/sysconfig/valheim-server",
        """SERVER_NAME=My Server
          |SERVER_PORT=2456
          |WORLD_NAME=Dedicated
          |SERVER_PASS=secret
          |SERVER_PUBLIC=true
          |DISCORD_WEBHOOK=https://discord.com/api/webhooks/930166932420829224/K7uJCinEs4PAEdRgJf07EXrOMiDpK7NmGJVSpxPtUdEvDYYehHwo0pan29nHpOO7U9db
          |POST_SERVER_LISTENING_HOOK=curl -sfSL -X POST -H "Content-Type: application/json" -d "{\"username\":\"Valheim\",\"content\":\"Valheim server started\"}" "$DISCORD_WEBHOOK"
          |PRE_SERVER_SHUTDOWN_HOOK=curl -sfSL -X POST -H "Content-Type: application/json" -d "{\"username\":\"Valheim\",\"content\":\"Valheim server shutting down\"}" "$DISCORD_WEBHOOK"
          |""".stripMargin),
      InitFile.fromUrl("/etc/systemd/system/valheim.service", "https://raw.githubusercontent.com/lloesche/valheim-server-docker/main/valheim.service"),
      InitCommand.shellCommand(
        """systemctl daemon-reload
          |systemctl enable valheim.service
          |systemctl start valheim.service
          |""".stripMargin)
    ))
    .build
  // allow incoming traffic
  instance.getConnections.allowFromAnyIpv4(Port.tcp(22), "ssh")
  instance.getConnections.allowFromAnyIpv4(Port.udpRange(2456, 2458), "valheim")
  // update policy
}
