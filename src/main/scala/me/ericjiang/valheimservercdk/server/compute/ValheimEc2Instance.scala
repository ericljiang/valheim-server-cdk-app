package me.ericjiang.valheimservercdk.server.compute

import software.amazon.awscdk.services.cloudwatch.Metric
import software.amazon.awscdk.services.ec2._
import software.amazon.awscdk.services.iam.{Effect, PolicyStatement}
import software.amazon.awscdk.services.s3.Bucket
import software.amazon.awscdk.{Duration, Stack, Stage}
import software.constructs.Construct

import scala.jdk.CollectionConverters._

class ValheimEc2Instance(scope: Construct, id: String) extends Construct(scope, id) {
  val backupBucket = new Bucket(this, "BackupBucket")

  // create instance
  val instance: Instance = Instance.Builder.create(this, "Instance")
    .instanceType(InstanceType.of(InstanceClass.BURSTABLE3_AMD, InstanceSize.MEDIUM))
    .machineImage(MachineImage.latestAmazonLinux(AmazonLinuxImageProps.builder
      .generation(AmazonLinuxGeneration.AMAZON_LINUX_2)
      .cachedInContext(true)
      .build))
    .vpc(Vpc.fromLookup(this, "DefaultVpc", VpcLookupOptions.builder.isDefault(true).build))
    .init(cloudFormationInit(
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

  val playerCountMetric: Metric = Metric.Builder.create
    .namespace("ValheimServer")
    .metricName("PlayerCount")
    .dimensionsMap(Map("Stage" -> Stage.of(this).getStageName).asJava)
    .period(Duration.minutes(5))
    .statistic("max")
    .build

  private def cloudFormationInit(stageName: String, backupBucketName: String, region: String): CloudFormationInit =
    CloudFormationInit.fromElements(
      InitPackage.yum("docker"),
      InitPackage.yum("jq"),
      // Environment variables used inside Docker container
      InitFile.fromString("/etc/sysconfig/valheim-server",
        raw"""SERVER_NAME=Yeah
             |SERVER_PORT=2456
             |WORLD_NAME=yeahh
             |SERVER_PASS=yeah1234
             |SERVER_PUBLIC=true
             |STATUS_HTTP=true
             |BACKUPS_CRON=*/30 * * * *
             |DISCORD_WEBHOOK=https://discord.com/api/webhooks/930203489722826813/9r6qTG5_n162Fb2u6yISOvDh9GZ2kVdXKvCWGYUMKHUjTuMfGXOTE58w2gwYYOnhuZGD
             |POST_BOOTSTRAP_HOOK=apt-get update && DEBIAN_FRONTEND=noninteractive apt-get -y install awscli
             |POST_SERVER_LISTENING_HOOK=curl -sfSL -X POST -H "Content-Type: application/json" -d "{\"username\":\"Valheim\",\"content\":\"Valheim server started\"}" "$$DISCORD_WEBHOOK"
             |POST_BACKUP_HOOK=aws s3 cp @BACKUP_FILE@ s3://$backupBucketName/
             |PRE_SERVER_SHUTDOWN_HOOK=supervisorctl signal HUP valheim-backup && sleep 60
             |POST_SERVER_SHUTDOWN_HOOK=aws s3 cp @BACKUP_FILE@ s3://$backupBucketName/
             |""".stripMargin),
      // Environment variables used in systemd unit that runs the Docker container
      InitFile.fromString("/etc/sysconfig/valheim-service",
        s"""STAGE_NAME=$stageName
           |""".stripMargin),
      InitFile.fromFileInline("/etc/systemd/system/valheim.service", "src/main/resources/valheim.service"),
      InitFile.fromFileInline("/usr/local/bin/put-player-count-metric.sh", "src/main/resources/put-player-count-metric.sh"),
      InitFile.fromString("/etc/cron.d/put-player-count-metric",
        s"""*/5 * * * * root REGION=$region STAGE_NAME=$stageName /usr/local/bin/put-player-count-metric.sh
           |""".stripMargin), // Newline required at end of file
      InitCommand.shellCommand(
        """systemctl daemon-reload
          |systemctl enable valheim.service
          |systemctl start valheim.service
          |""".stripMargin)
    )
}
