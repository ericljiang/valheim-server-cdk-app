package me.ericjiang.valheimservercdk.server.automation.api

import me.ericjiang.valheimservercdk.server.compute.AutoStoppingGameServer
import software.amazon.awscdk.{Duration, Stage}
import software.amazon.awscdk.services.apigatewayv2.alpha.{AddRoutesOptions, CorsHttpMethod, CorsPreflightOptions, HttpApi, HttpMethod}
import software.amazon.awscdk.services.apigatewayv2.integrations.alpha.HttpLambdaIntegration
import software.constructs.Construct

import scala.jdk.CollectionConverters._

/**
 * Serverless API that the client interacts with.
 */
class ClientApi(scope: Construct, id: String, server: AutoStoppingGameServer) extends Construct(scope, id) {
  val api: HttpApi = HttpApi.Builder.create(this, "HttpApi")
    .description(Stage.of(this).getStageName)
    .corsPreflight(CorsPreflightOptions.builder
      .allowHeaders(List("Authorization").asJava)
      .allowMethods(List(CorsHttpMethod.GET, CorsHttpMethod.HEAD, CorsHttpMethod.OPTIONS, CorsHttpMethod.POST).asJava)
      .allowOrigins(List("*").asJava)
      .maxAge(Duration.days(10))
      .build)
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
}
