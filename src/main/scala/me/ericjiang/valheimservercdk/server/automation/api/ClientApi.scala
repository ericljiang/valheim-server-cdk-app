package me.ericjiang.valheimservercdk.server.automation.api

import me.ericjiang.valheimservercdk.server.automation.api.ClientApi._
import me.ericjiang.valheimservercdk.server.compute.AutoStoppingGameServer
import software.amazon.awscdk.services.apigatewayv2.alpha._
import software.amazon.awscdk.services.apigatewayv2.integrations.alpha.HttpLambdaIntegration
import software.amazon.awscdk.services.certificatemanager.{Certificate, CertificateValidation}
import software.amazon.awscdk.services.route53.targets.ApiGatewayv2DomainProperties
import software.amazon.awscdk.services.route53.{ARecord, HostedZone, HostedZoneAttributes, RecordTarget}
import software.amazon.awscdk.{Duration, Stage}
import software.constructs.Construct

import scala.jdk.CollectionConverters._

/**
 * Serverless API that the client interacts with.
 */
class ClientApi(scope: Construct, id: String, server: AutoStoppingGameServer) extends Construct(scope, id) {
  // Import existing HZ not defined in this CDK app
  private val hostedZone = HostedZone.fromHostedZoneAttributes(this, "HostedZone", HostedZoneAttributes.builder
    .zoneName(zoneName)
    .hostedZoneId(hostedZoneId)
    .build)
  private val certificate = Certificate.Builder.create(this, "Certificate")
    .domainName(domainName)
    .validation(CertificateValidation.fromDns(hostedZone))
    .build
  private val apiDomainName = DomainName.Builder.create(this, "DomainName")
    .domainName(domainName)
    .certificate(certificate)
    .build
  ARecord.Builder.create(this, "ARecord")
    .zone(hostedZone)
    .recordName(domainName)
    .target(RecordTarget.fromAlias(new ApiGatewayv2DomainProperties(
      apiDomainName.getRegionalDomainName,
      apiDomainName.getRegionalHostedZoneId)))
    .build

  val api: HttpApi = HttpApi.Builder.create(this, "HttpApi")
    .description(Stage.of(this).getStageName)
    .defaultDomainMapping(DomainMappingOptions.builder
      .domainName(apiDomainName)
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

  def endpoint: String = api.getApiEndpoint
}

object ClientApi {
  private val zoneName = "ericjiang.me"
  private val hostedZoneId = "Z06067853SHQF3QW16T9N"
  private val domainName = s"api.valheim.$zoneName"
}
