package me.ericjiang.valheimservercdk.server.automation.api

import me.ericjiang.valheimservercdk.StageConfig
import me.ericjiang.valheimservercdk.server.compute.AutoStoppingGameServer
import software.amazon.awscdk.Duration
import software.amazon.awscdk.services.apigatewayv2.alpha._
import software.amazon.awscdk.services.apigatewayv2.integrations.alpha.HttpLambdaIntegration
import software.constructs.Construct

import scala.jdk.CollectionConverters._

/**
 * Serverless API that the client interacts with.
 */
class ClientApi(scope: Construct, id: String, server: AutoStoppingGameServer) extends Construct(scope, id) {
  private val stageConfig: StageConfig = StageConfig.find(this)

  private val api: HttpApi = HttpApi.Builder.create(this, "HttpApi")
    .description(stageConfig.stageName)
    .corsPreflight(CorsPreflightOptions.builder
      .allowHeaders(List("Authorization").asJava)
      .allowMethods(List(CorsHttpMethod.GET, CorsHttpMethod.HEAD, CorsHttpMethod.OPTIONS, CorsHttpMethod.POST).asJava)
      .allowOrigins(List("*").asJava)
      .maxAge(Duration.days(10))
      .build)
    .createDefaultStage(false)
    .build
  api.addRoutes(AddRoutesOptions.builder
    .path("/start-server")
    .methods(List(HttpMethod.POST).asJava)
    .integration(new HttpLambdaIntegration("StartServerIntegration", server.startFunction))
    .build)
  api.addRoutes(AddRoutesOptions.builder
    .path("/get-server-status")
    .methods(List(HttpMethod.POST).asJava)
    .integration(new HttpLambdaIntegration("GetServerStatusIntegration", server.statusFunction))
    .build)

  private val stage: HttpStage = HttpStage.Builder.create(this, "Stage")
    .httpApi(api)
    .stageName(stageConfig.apiPath)
    .autoDeploy(true)
    .build

  def getUrl: String = stage.getUrl
}
