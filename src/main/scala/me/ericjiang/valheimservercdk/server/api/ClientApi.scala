package me.ericjiang.valheimservercdk.server.api

import software.amazon.awscdk.Duration
import software.amazon.awscdk.services.apigatewayv2.alpha.{AddRoutesOptions, CorsHttpMethod, CorsPreflightOptions, HttpApi, HttpMethod}
import software.amazon.awscdk.services.apigatewayv2.integrations.alpha.HttpLambdaIntegration
import software.amazon.awscdk.services.lambda.{Function, InlineCode, Runtime}
import software.constructs.Construct

import scala.jdk.CollectionConverters._

/**
 * Serverless API that the client interacts with.
 */
class ClientApi(scope: Construct, id: String) extends Construct(scope, id) {
  private val startServerFunction = Function.Builder.create(this, "StartServer")
    .runtime(Runtime.NODEJS_12_X)
    .handler("index.handler")
    .code(new InlineCode(
      """exports.handler = async (event) => {
        |    const response = {
        |        statusCode: 200,
        |        body: JSON.stringify('Hello from Lambda!'),
        |    };
        |    return response;
        |};
        |""".stripMargin))
    .build
  private val getServerStatusFunction = Function.Builder.create(this, "GetServerStatus")
    .runtime(Runtime.NODEJS_12_X)
    .handler("index.handler")
    .code(new InlineCode(
      """exports.handler = async (event) => {
        |    const response = {
        |        statusCode: 200,
        |        body: JSON.stringify('Hello from Lambda!'),
        |    };
        |    return response;
        |};
        |""".stripMargin))
    .build

  private val api = HttpApi.Builder.create(this, "HttpApi")
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
    .integration(new HttpLambdaIntegration("StartServerIntegration", startServerFunction))
    .build)
  api.addRoutes(AddRoutesOptions.builder
    .path("/get-server-status")
    .methods(List(HttpMethod.POST).asJava)
    .integration(new HttpLambdaIntegration("GetServerStatusIntegration", getServerStatusFunction))
    .build)
}
