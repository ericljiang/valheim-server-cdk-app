package me.ericjiang.valheimservercdk.server.compute
import software.amazon.awscdk.services.cloudwatch.Alarm
import software.amazon.awscdk.services.lambda
import software.constructs.Construct

class AutomatableValheimServer(scope: Construct, id: String) extends Construct(scope, id) with AutomatableGameServer {

  val valheimInstance = new ValheimEc2Instance(this, "Instance")

  override val startFunction: lambda.Function =
    new StartEc2InstanceFunction(this, "StartFunction", valheimInstance.instance).function

  override val stopFunction: lambda.Function =
    new StopEc2InstanceFunction(this, "StopFunction", valheimInstance.instance).function

  override def statusFunction: lambda.Function = ???

  override val idleAlarm: Alarm =
    new PlayerCountBasedIdleAlarm(this, "IdleAlarm", valheimInstance.playerCountMetric).alarm
}
