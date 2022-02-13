package me.ericjiang.valheimservercdk.server.compute.ec2

import me.ericjiang.valheimservercdk.util.CdkUtils.InstanceExtensions
import software.amazon.awscdk.services.ec2.Instance
import software.amazon.awscdk.services.iam.{Effect, PolicyStatement}
import software.amazon.awscdk.services.lambda.{Code, Function, Runtime}
import software.constructs.Construct

import scala.jdk.CollectionConverters._

class StopEc2InstanceFunction(scope: Construct, id: String, instance: Instance) extends Construct(scope, id) {
  val function: Function = Function.Builder.create(this, "StopServer")
    .runtime(Runtime.NODEJS_14_X)
    .handler("index.handler")
    .code(Code.fromAsset("src/main/resources/lambda/stop-instance"))
    .environment(Map("INSTANCE_ID" -> instance.getInstanceId).asJava)
    .build
  function.addToRolePolicy(PolicyStatement.Builder.create
    .actions(Seq("ec2:StopInstances").asJava)
    .effect(Effect.ALLOW)
    .resources(Seq(instance.getArn).asJava)
    .build)
}
