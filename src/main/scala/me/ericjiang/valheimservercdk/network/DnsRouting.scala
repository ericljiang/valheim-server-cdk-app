package me.ericjiang.valheimservercdk.network

import me.ericjiang.valheimservercdk.StageConfig
import me.ericjiang.valheimservercdk.server.compute.AutomatableGameServer
import software.amazon.awscdk.services.certificatemanager.DnsValidatedCertificate
import software.amazon.awscdk.services.cloudfront._
import software.amazon.awscdk.services.cloudfront.origins.{HttpOrigin, S3Origin}
import software.amazon.awscdk.services.cloudtrail.Trail
import software.amazon.awscdk.services.events.OnEventOptions
import software.amazon.awscdk.services.events.targets.LambdaFunction
import software.amazon.awscdk.services.route53._
import software.amazon.awscdk.services.route53.targets.CloudFrontTarget
import software.amazon.awscdk.services.s3.IBucket
import software.constructs.Construct

import scala.jdk.CollectionConverters._

class DnsRouting(scope: Construct, id: String, websiteBucket: IBucket, apiDomain: String, gameServer: AutomatableGameServer)
  extends Construct(scope, id) {

  private val stageConfig = StageConfig.find(this)

  private val hostedZone = HostedZone.fromHostedZoneAttributes(this, "HostedZone", HostedZoneAttributes.builder
    .zoneName("ericjiang.me")
    .hostedZoneId("Z06067853SHQF3QW16T9N")
    .build)

  private val certificate = DnsValidatedCertificate.Builder.create(this, "Certificate")
    .domainName(stageConfig.appDomain)
    .hostedZone(hostedZone)
    .region("us-east-1")
    .build

  private val distribution = Distribution.Builder.create(this, "CloudFrontDistribution")
    .defaultBehavior(BehaviorOptions.builder
      .origin(new S3Origin(websiteBucket))
      .cachePolicy(CachePolicy.CACHING_DISABLED) // TODO figure out a way to cache and invalidate on deployment
      .viewerProtocolPolicy(ViewerProtocolPolicy.REDIRECT_TO_HTTPS)
      .build)
    .additionalBehaviors(Map(
      s"/${stageConfig.apiPath}/*" -> BehaviorOptions.builder
        .origin(new HttpOrigin(apiDomain))
        .cachePolicy(CachePolicy.CACHING_DISABLED)
        .allowedMethods(AllowedMethods.ALLOW_ALL)
        .build
    ).asJava)
    .domainNames(Seq(stageConfig.appDomain).asJava)
    .certificate(certificate)
    .build

  ARecord.Builder.create(this, "ARecord")
    .zone(hostedZone)
    .recordName(stageConfig.appDomain)
    .target(RecordTarget.fromAlias(new CloudFrontTarget(distribution)))
    .build

  private val routeDnsFunction = new RouteDnsFunction(this, "RouteDnsFunction", hostedZone, gameServer.statusFunction)

  Trail.onEvent(this, "RouteDnsRule", OnEventOptions.builder
    .eventPattern(gameServer.startEventPattern)
    .target(new LambdaFunction(routeDnsFunction.function))
    .build)
}
