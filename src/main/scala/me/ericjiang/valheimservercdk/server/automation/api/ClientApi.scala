package me.ericjiang.valheimservercdk.server.automation.api

import me.ericjiang.valheimservercdk.StageConfig
import me.ericjiang.valheimservercdk.server.automation.api.ClientApi._
import me.ericjiang.valheimservercdk.server.compute.AutoStoppingGameServer
import software.amazon.awscdk.Duration
import software.amazon.awscdk.services.apigatewayv2.alpha._
import software.amazon.awscdk.services.apigatewayv2.integrations.alpha.HttpLambdaIntegration
import software.amazon.awscdk.services.certificatemanager.{Certificate, CertificateValidation}
import software.amazon.awscdk.services.route53.targets.ApiGatewayv2DomainProperties
import software.amazon.awscdk.services.route53.{ARecord, IHostedZone, RecordTarget}
import software.constructs.Construct

import scala.jdk.CollectionConverters._

/**
 * Serverless API that the client interacts with.
 */
class ClientApi(scope: Construct, id: String, server: AutoStoppingGameServer, hostedZone: IHostedZone) extends Construct(scope, id) {

  private val appDomain = StageConfig.find(this).appDomain
  private val apiDomain = s"api.$appDomain"

  private val certificate = Certificate.Builder.create(this, "Certificate")
    .domainName(apiDomain)
    .validation(CertificateValidation.fromDns(hostedZone))
    .build
  private val customDomain = DomainName.Builder.create(this, "DomainName")
    .domainName(apiDomain)
    .certificate(certificate)
    .build
  ARecord.Builder.create(this, "ARecord")
    .zone(hostedZone)
    .recordName(apiDomain)
    .target(RecordTarget.fromAlias(new ApiGatewayv2DomainProperties(
      customDomain.getRegionalDomainName,
      customDomain.getRegionalHostedZoneId)))
    .build

  val api: HttpApi = HttpApi.Builder.create(this, "HttpApi")
    .description(StageConfig.find(this).stageName)
    .defaultDomainMapping(DomainMappingOptions.builder
      .domainName(customDomain)
      .build)
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

object ClientApi {
  private val zoneName = "ericjiang.me"
  private val hostedZoneId = "Z06067853SHQF3QW16T9N"
}
