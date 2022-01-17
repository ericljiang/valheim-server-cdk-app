package me.ericjiang.valheimservercdk.server.compute

import me.ericjiang.valheimservercdk.util.CdkUtils.InstanceExtensions
import software.amazon.awscdk.services.ec2.Instance
import software.amazon.awscdk.services.iam.{Effect, PolicyStatement}
import software.amazon.awscdk.services.lambda.{Function, InlineCode, Runtime}
import software.constructs.Construct

import scala.jdk.CollectionConverters._

class StopEc2InstanceFunction(scope: Construct, id: String, instance: Instance) extends Construct(scope, id) {
  val function: Function = Function.Builder.create(this, "StopServer")
    .runtime(Runtime.NODEJS_14_X)
    .handler("index.handler")
    .code(new InlineCode(
      """const EC2 = require('aws-sdk/clients/ec2');
        |const ec2 = new EC2();
        |const params = { InstanceIds: [process.env.INSTANCE_ID] };
        |exports.handler = async (event) => await ec2.stopInstances(params).promise();
        |""".stripMargin))
    .environment(Map("INSTANCE_ID" -> instance.getInstanceId).asJava)
    .build
  function.addToRolePolicy(PolicyStatement.Builder.create
    .actions(Seq("ec2:StopInstances").asJava)
    .effect(Effect.ALLOW)
    .resources(Seq(instance.getArn).asJava)
    .build)
}
