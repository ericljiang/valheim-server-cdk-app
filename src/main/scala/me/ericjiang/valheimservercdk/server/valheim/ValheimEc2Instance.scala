package me.ericjiang.valheimservercdk.server.valheim

import me.ericjiang.valheimservercdk.StageConfig
import software.amazon.awscdk.Duration
import software.amazon.awscdk.services.cloudwatch.Metric
import software.amazon.awscdk.services.ec2._
import software.amazon.awscdk.services.iam.{Effect, ManagedPolicy, PolicyStatement}
import software.amazon.awscdk.services.s3.Bucket
import software.constructs.Construct

import scala.io.Source
import scala.jdk.CollectionConverters._
import scala.util.Using

class ValheimEc2Instance(scope: Construct, id: String) extends Construct(scope, id) {

  private val stageConfig: StageConfig = StageConfig.find(this)

  private val backupBucket = new Bucket(this, "BackupBucket")

  private val cloudFormationInit = CloudFormationInit.fromElements(
    InitPackage.yum("amazon-cloudwatch-agent"),
    InitPackage.yum("docker"),
    InitPackage.yum("jq"),
    InitPackage.yum("nmap-ncat"),
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
           |POST_BACKUP_HOOK=aws s3 cp @BACKUP_FILE@ s3://${backupBucket.getBucketName}/
           |PRE_SERVER_SHUTDOWN_HOOK=supervisorctl signal HUP valheim-backup && sleep 60
           |POST_SERVER_SHUTDOWN_HOOK=aws s3 cp @BACKUP_FILE@ s3://${backupBucket.getBucketName}/
           |""".stripMargin),
    // Environment variables used in systemd unit that runs the Docker container
    InitFile.fromString("/etc/sysconfig/valheim-service",
      s"""LOG_GROUP=${stageConfig.logGroup}
         |""".stripMargin),
    InitFile.fromFileInline("/etc/systemd/system/valheim.service", "src/main/resources/ec2/valheim.service"),
    InitFile.fromFileInline(
      "/usr/local/bin/put-player-count-metric.sh",
      "src/main/resources/ec2/put-player-count-metric.sh",
      InitFileOptions.builder.mode("000744").build),
    InitFile.fromFileInline(
      "/usr/local/bin/put-uptime-metric.sh",
      "src/main/resources/ec2/put-uptime-metric.sh",
      InitFileOptions.builder.mode("000744").build),
    InitFile.fromString("/etc/cron.d/metrics",
      """*/5 * * * * root /usr/local/bin/put-player-count-metric.sh
        |*/5 * * * * root /usr/local/bin/put-uptime-metric.sh
        |""".stripMargin), // Newline required at end of file
    InitFile.fromString("/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json",
      raw"""{
           |  "metrics": {
           |    "namespace": "${stageConfig.metricNamespace}",
           |    "append_dimensions": {
           |      "InstanceId": "$${aws:InstanceId}",
           |      "InstanceType": "$${aws:InstanceType}"
           |    },
           |    "aggregation_dimensions": [["InstanceId", "InstanceType"], ["InstanceId"]],
           |    "metrics_collected": {
           |      "cpu": {
           |        "resources": ["*"],
           |        "totalcpu": true,
           |        "measurement": ["cpu_usage_active"]
           |      },
           |      "statsd": {}
           |    }
           |  }
           |}
           |""".stripMargin),
    InitCommand.shellCommand(
      Using(Source.fromResource("ec2/init.sh"))(_.mkString).get,
      InitCommandOptions.builder
        .env(Map("LOG_GROUP" -> stageConfig.logGroup).asJava)
        .build
    )
  )

  // create instance
  val instance: Instance = Instance.Builder.create(this, "Instance")
    .instanceType(InstanceType.of(InstanceClass.BURSTABLE3_AMD, InstanceSize.MEDIUM))
    .machineImage(MachineImage.latestAmazonLinux(AmazonLinuxImageProps.builder
      .generation(AmazonLinuxGeneration.AMAZON_LINUX_2)
      .cachedInContext(true)
      .build))
    .vpc(Vpc.fromLookup(this, "DefaultVpc", VpcLookupOptions.builder.isDefault(true).build))
    .init(cloudFormationInit)
    // Recreate instance to apply changes. Use in development only!
    .initOptions(ApplyCloudFormationInitOptions.builder
      .printLog(true)
      .ignoreFailures(!stageConfig.rollbackInstanceOnFailure)
      .embedFingerprint(true).build)
    .userDataCausesReplacement(true)
    .build

  // allow incoming traffic
  instance.getConnections.allowFromAnyIpv4(Port.tcp(22), "ssh")
  instance.getConnections.allowFromAnyIpv4(Port.udpRange(2456, 2457), "valheim")
  instance.getConnections.allowFromAnyIpv4(Port.tcp(80), "Status API")
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
  instance.getRole.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName("CloudWatchAgentServerPolicy"))

  val playerCountMetric: Metric = Metric.Builder.create
    .namespace(stageConfig.metricNamespace)
    .metricName("PlayerCount")
    .dimensionsMap(Map("InstanceId" -> instance.getInstanceId).asJava)
    .period(Duration.minutes(5))
    .statistic("max")
    .build

  val uptimeMetric: Metric = Metric.Builder.create
    .namespace(stageConfig.metricNamespace)
    .metricName("Uptime")
    .dimensionsMap(Map("InstanceId" -> instance.getInstanceId).asJava)
    .period(Duration.minutes(5))
    .statistic("max")
    .build
}
