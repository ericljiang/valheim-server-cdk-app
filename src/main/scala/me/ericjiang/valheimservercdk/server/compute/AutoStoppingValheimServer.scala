package me.ericjiang.valheimservercdk.server.compute

import software.amazon.awscdk.services.cloudwatch.actions.{Ec2Action, Ec2InstanceAction}
import software.amazon.awscdk.services.lambda
import software.constructs.Construct

import scala.concurrent.duration.Duration

class AutoStoppingValheimServer(scope: Construct, id: String, idleDuration: Duration)
  extends Construct(scope, id) with AutoStoppingGameServer {

  private val valheimInstance = new ValheimEc2Instance(this, "Instance")

  private val idleAlarm = new PlayerCountBasedIdleAlarm(this, "IdleAlarm",
    playerCountMetric = valheimInstance.playerCountMetric,
    idleDuration = idleDuration)
  idleAlarm.alarm.addAlarmAction(new Ec2Action(Ec2InstanceAction.STOP))

  override val startFunction: lambda.Function =
    new StartEc2InstanceFunction(this, "StartFunction", valheimInstance.instance).function

  override def statusFunction: lambda.Function = ???
}
