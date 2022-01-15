package me.ericjiang.valheimservercdk.server.compute

import me.ericjiang.valheimservercdk.util.CdkUtils.InstanceExtensions
import software.amazon.awscdk.services.cloudwatch.{Alarm, ComparisonOperator, Metric}
import software.amazon.awscdk.services.ec2._
import software.amazon.awscdk.services.iam.{Effect, PolicyStatement}
import software.amazon.awscdk.services.lambda
import software.amazon.awscdk.services.lambda.{Function, InlineCode, Runtime}
import software.amazon.awscdk.services.s3.Bucket
import software.amazon.awscdk.{Duration, Stack, Stage}
import software.constructs.Construct

import scala.jdk.CollectionConverters._

class ValheimEc2Instance(scope: Construct, id: String) extends Construct(scope, id) with AutomatableGameServer {
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

  override def startFunction: lambda.Function = ???

  override val stopFunction: lambda.Function = {
    val function = Function.Builder.create(this, "StopServer")
      .runtime(Runtime.NODEJS_14_X)
      .handler("index.handler")
      .code(new InlineCode(
        """const { EC2Client, StopInstancesCommand } = require("@aws-sdk/client-ec2");
          |const client = new EC2Client(config);
          |const command = new StopInstancesCommand({
          |    InstanceIds: [process.env.INSTANCE_ID]
          |});
          |exports.handler = async (event) => await client.send(command);
          |""".stripMargin))
      .environment(Map("INSTANCE_ID" -> instance.getInstanceId).asJava)
      .build
    function.addToRolePolicy(PolicyStatement.Builder.create
      .actions(Seq().asJava)
      .effect(Effect.ALLOW)
      .resources(Seq(instance.getArn).asJava)
      .build)
    function
  }

  override def statusFunction: lambda.Function = ???

  override val idleAlarm: Alarm = Alarm.Builder.create(this, "IdleAlarm")
    .alarmDescription("Indicates that the server is idle and can be shut down.")
    .metric(Metric.Builder.create
      .namespace("ValheimServer")
      .metricName("PlayerCount")
      .dimensionsMap(Map("Stage" -> Stage.of(this).getStageName).asJava)
      .period(Duration.minutes(5))
      .statistic("max")
      .build)
    .comparisonOperator(ComparisonOperator.LESS_THAN_OR_EQUAL_TO_THRESHOLD)
    .threshold(0)
    .evaluationPeriods(12)
    .build
}
