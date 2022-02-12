package me.ericjiang.valheimservercdk.server.compute

import me.ericjiang.valheimservercdk.util.CdkUtils.InstanceExtensions
import software.amazon.awscdk.services.cloudwatch.actions.{Ec2Action, Ec2InstanceAction}
import software.amazon.awscdk.services.events.EventPattern
import software.amazon.awscdk.services.lambda
import software.constructs.Construct

import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters._

class AutoStoppingValheimServer(scope: Construct, id: String, idleDuration: Duration)
  extends Construct(scope, id) with AutoStoppingGameServer {

  private val valheimInstance = new ValheimEc2Instance(this, "Instance")

  private val idleAlarm = new PlayerCountBasedIdleAlarm(this, "IdleAlarm",
    playerCountMetric = valheimInstance.playerCountMetric,
    idleDuration = idleDuration)
  idleAlarm.alarm.addAlarmAction(new Ec2Action(Ec2InstanceAction.STOP))

  override val startFunction: lambda.Function =
    new StartEc2InstanceFunction(this, "StartFunction", valheimInstance.instance).function

  override val statusFunction: lambda.Function =
    new ValheimStatusFunction(this, "StatusFunction", valheimInstance.instance).function

  override def getIpAddress: lambda.Function = ???

  override val startEventPattern: EventPattern = EventPattern.builder
    .source(Seq("aws.ec2").asJava)
    .detailType(Seq("EC2 Instance State-change Notification").asJava)
    .resources(Seq(valheimInstance.instance.getArn).asJava)
    .detail(Map(
      "instance-id" -> Seq(valheimInstance.instance.getInstanceId).asJava,
      "state" -> Seq("running").asJava
    ).asJava)
    .build
}
