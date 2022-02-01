package me.ericjiang.valheimservercdk.server.compute

import me.ericjiang.valheimservercdk.StageConfig
import software.amazon.awscdk.services.ec2.Instance
import software.amazon.awscdk.services.iam.{Effect, PolicyStatement}
import software.amazon.awscdk.services.lambda.{Code, Function, Runtime}
import software.amazon.awscdk.services.route53.IHostedZone
import software.constructs.Construct

import scala.jdk.CollectionConverters._

class RouteDnsToEc2Function(scope: Construct, id: String, instance: Instance, hostedZone: IHostedZone)
  extends Construct(scope, id) {
  val function: Function = Function.Builder.create(this, "RouteDns")
    .runtime(Runtime.NODEJS_14_X)
    .handler("index.handler")
    .code(Code.fromAsset("src/main/resources/lambda/dns"))
    .environment(Map(
      "INSTANCE_ID" -> instance.getInstanceId,
      "RECORD_NAME" -> StageConfig.find(this).gameServerDomain,
      "HOSTED_ZONE_ID" -> hostedZone.getHostedZoneId
    ).asJava)
    .build
  function.addToRolePolicy(PolicyStatement.Builder.create
    .actions(Seq("ec2:DescribeInstances").asJava)
    .effect(Effect.ALLOW)
    .resources(Seq("*").asJava) // DescribeInstances does not support individual resources
    .build)
  function.addToRolePolicy(PolicyStatement.Builder.create
    .actions(Seq("route53:ChangeResourceRecordSets").asJava)
    .effect(Effect.ALLOW)
    .resources(Seq(hostedZone.getHostedZoneArn).asJava)
    .build)
}
