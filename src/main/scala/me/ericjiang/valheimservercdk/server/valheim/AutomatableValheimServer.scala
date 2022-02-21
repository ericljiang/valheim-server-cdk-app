package me.ericjiang.valheimservercdk.server.valheim

import me.ericjiang.valheimservercdk.server.compute.ec2.{StartEc2InstanceFunction, StopEc2InstanceFunction}
import me.ericjiang.valheimservercdk.server.compute.{AutomatableGameServer, PlayerCountBasedIdleAlarm}
import me.ericjiang.valheimservercdk.util.CdkUtils.InstanceExtensions
import software.amazon.awscdk.services.cloudwatch.{Alarm, Metric}
import software.amazon.awscdk.services.events.EventPattern
import software.amazon.awscdk.services.lambda
import software.constructs.Construct

import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters._

class AutomatableValheimServer(scope: Construct, id: String, idleDuration: Duration)
  extends Construct(scope, id) with AutomatableGameServer {

  private val valheimInstance = new ValheimEc2Instance(this, "Instance")

  override val startFunction: lambda.Function =
    new StartEc2InstanceFunction(this, "StartFunction", valheimInstance.instance).function

  override val stopFunction: lambda.Function =
    new StopEc2InstanceFunction(this, "StopFunction", valheimInstance.instance).function

  override val statusFunction: lambda.Function =
    new ValheimStatusFunction(this, "StatusFunction", valheimInstance.instance).function

  override val startEventPattern: EventPattern = EventPattern.builder
    .source(Seq("aws.ec2").asJava)
    .detailType(Seq("EC2 Instance State-change Notification").asJava)
    .resources(Seq(valheimInstance.instance.getArn).asJava)
    .detail(Map(
      "instance-id" -> Seq(valheimInstance.instance.getInstanceId).asJava,
      "state" -> Seq("running").asJava
    ).asJava)
    .build

  override val idleAlarm: Alarm = new PlayerCountBasedIdleAlarm(this, "IdleAlarm",
    playerCountMetric = valheimInstance.playerCountMetric,
    idleDuration = idleDuration
  ).alarm

  override val uptimeMetric: Metric = valheimInstance.uptimeMetric
}
