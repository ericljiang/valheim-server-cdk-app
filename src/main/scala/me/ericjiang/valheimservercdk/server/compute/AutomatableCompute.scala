package me.ericjiang.valheimservercdk.server.compute

import software.amazon.awscdk.{Stack, Stage}
import software.amazon.awscdk.services.ec2._
import software.amazon.awscdk.services.iam.{Effect, PolicyStatement}
import software.amazon.awscdk.services.s3.Bucket
import software.constructs.Construct

import scala.jdk.CollectionConverters.SeqHasAsJava

class AutomatableCompute(scope: Construct, id: String) extends Construct(scope, id) {
  private val backupBucket = new Bucket(this, "BackupBucket")

  // create instance
  private val instance = Instance.Builder.create(this, "Instance")
    .instanceType(InstanceType.of(InstanceClass.BURSTABLE3_AMD, InstanceSize.MEDIUM))
    .machineImage(MachineImage.latestAmazonLinux(AmazonLinuxImageProps.builder
      .generation(AmazonLinuxGeneration.AMAZON_LINUX_2)
      .cachedInContext(true)
      .build))
    .vpc(Vpc.fromLookup(this, "DefaultVpc", VpcLookupOptions.builder.isDefault(true).build))
    .init(ValheimCloudFormationInit(
      stageName = Stage.of(this).getStageName,
      backupBucketName = backupBucket.getBucketName,
      region = Stack.of(this).getRegion))
    // Recreate instance to apply changes. Use in development only!
    .initOptions(ApplyCloudFormationInitOptions.builder.embedFingerprint(true).build)
    .userDataCausesReplacement(true)
    .build

  // allow incoming traffic
  instance.getConnections.allowFromAnyIpv4(Port.tcp(22), "ssh")
  instance.getConnections.allowFromAnyIpv4(Port.udpRange(2456, 2457), "valheim")
  // permissions
  backupBucket.grantPut(instance)
  instance.addToRolePolicy(PolicyStatement.Builder.create
    .actions(Seq("logs:CreateLogGroup", "logs:CreateLogStream", "logs:PutLogEvents").asJava)
    .effect(Effect.ALLOW)
    .resources(Seq("*").asJava)
    .build)
  instance.addToRolePolicy(PolicyStatement.Builder.create
    .actions(Seq("cloudwatch:PutMetricData").asJava)
    .effect(Effect.ALLOW)
    .resources(Seq("*").asJava)
    .build)
}
