package me.ericjiang.valheimservercdk.network

import me.ericjiang.valheimservercdk.StageConfig
import software.amazon.awscdk.services.iam.{Effect, PolicyStatement}
import software.amazon.awscdk.services.lambda.{Code, Function, Runtime}
import software.amazon.awscdk.services.route53.HostedZone
import software.constructs.Construct

import scala.jdk.CollectionConverters._

class RouteDnsFunction(scope: Construct, id: String, hostedZone: HostedZone)
  extends Construct(scope, id) {
  val function: Function = Function.Builder.create(this, "RouteDns")
    .runtime(Runtime.NODEJS_14_X)
    .handler("index.handler")
    .code(Code.fromAsset("src/main/resources/lambda/dns"))
    .environment(Map(
      "RECORD_NAME" -> StageConfig.find(this).gameServerDomain,
      "HOSTED_ZONE_ID" -> hostedZone.getHostedZoneId
    ).asJava)
    .build
  function.addToRolePolicy(PolicyStatement.Builder.create
    .actions(Seq("route53:ChangeResourceRecordSets").asJava)
    .effect(Effect.ALLOW)
    .resources(Seq(hostedZone.getHostedZoneArn).asJava)
    .build)
}