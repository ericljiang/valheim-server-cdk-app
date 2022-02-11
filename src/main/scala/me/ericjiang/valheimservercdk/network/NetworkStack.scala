package me.ericjiang.valheimservercdk.network

import me.ericjiang.valheimservercdk.StageConfig
import me.ericjiang.valheimservercdk.server.automation.api.ClientApi
import software.amazon.awscdk.services.certificatemanager.{DnsValidatedCertificate, ICertificate}
import software.amazon.awscdk.services.cloudfront.origins.{HttpOrigin, S3Origin}
import software.amazon.awscdk.services.cloudfront.{BehaviorOptions, CachePolicy, Distribution, ViewerProtocolPolicy}
import software.amazon.awscdk.services.route53._
import software.amazon.awscdk.services.route53.targets.CloudFrontTarget
import software.amazon.awscdk.services.s3.IBucket
import software.amazon.awscdk.{Stack, StackProps}
import software.constructs.Construct

import scala.jdk.CollectionConverters._

class NetworkStack(scope: Construct, id: String, props: StackProps = null, websiteBucket: IBucket, api: ClientApi)
  extends Stack(scope, id, props) {

  private val stageConfig: StageConfig = StageConfig.find(this)

  val hostedZone: IHostedZone = HostedZone.fromHostedZoneAttributes(this, "HostedZone", HostedZoneAttributes.builder
    .zoneName("ericjiang.me")
    .hostedZoneId("Z06067853SHQF3QW16T9N")
    .build)

  val certificate: ICertificate = DnsValidatedCertificate.Builder.create(this, "Certificate")
    .domainName(stageConfig.appDomain)
    .hostedZone(hostedZone)
    .region("us-east-1")
    .build

  val distribution: Distribution = Distribution.Builder.create(this, "CloudFrontDistribution")
    .defaultBehavior(BehaviorOptions.builder
      .origin(new S3Origin(websiteBucket))
      .cachePolicy(CachePolicy.CACHING_DISABLED) // TODO figure out a way to cache and invalidate on deployment
      .viewerProtocolPolicy(ViewerProtocolPolicy.REDIRECT_TO_HTTPS)
      .build)
    .additionalBehaviors(Map(
      s"/${stageConfig.apiPath}/*" -> BehaviorOptions.builder
        .origin(new HttpOrigin(api.getUrl))
        .cachePolicy(CachePolicy.CACHING_DISABLED)
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
}
