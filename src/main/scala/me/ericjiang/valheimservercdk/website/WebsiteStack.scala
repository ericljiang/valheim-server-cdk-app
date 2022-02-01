package me.ericjiang.valheimservercdk.website

import me.ericjiang.valheimservercdk.StageConfig
import software.amazon.awscdk.services.certificatemanager.DnsValidatedCertificate
import software.amazon.awscdk.services.cloudfront.origins.S3Origin
import software.amazon.awscdk.services.cloudfront.{BehaviorOptions, Distribution, ViewerProtocolPolicy}
import software.amazon.awscdk.services.route53.targets.CloudFrontTarget
import software.amazon.awscdk.services.route53.{ARecord, IHostedZone, RecordTarget}
import software.amazon.awscdk.services.s3.Bucket
import software.amazon.awscdk.services.s3.deployment.{BucketDeployment, Source}
import software.amazon.awscdk.{RemovalPolicy, Stack, StackProps}
import software.constructs.Construct

import scala.jdk.CollectionConverters._

class WebsiteStack(scope: Construct, id: String, props: StackProps = null, hostedZone: IHostedZone)
  extends Stack(scope, id, props) {

  private val stageConfig: StageConfig = StageConfig.find(this)

  private val websiteBucket = Bucket.Builder.create(this, "WebsiteBucket")
    .websiteIndexDocument("index.html")
    .publicReadAccess(true)
    .removalPolicy(RemovalPolicy.DESTROY)
    .autoDeleteObjects(true)
    .build

  private val certificate = DnsValidatedCertificate.Builder.create(this, "Certificate")
    .domainName(stageConfig.appDomain)
    .hostedZone(hostedZone)
    .region("us-east-1")
    .build

  private val distribution = Distribution.Builder.create(this, "CloudFrontDistribution")
    .defaultBehavior(BehaviorOptions.builder
      .origin(new S3Origin(websiteBucket))
      .viewerProtocolPolicy(ViewerProtocolPolicy.REDIRECT_TO_HTTPS)
      .build)
    .domainNames(Seq(stageConfig.appDomain).asJava)
    .certificate(certificate)
    .build

  BucketDeployment.Builder.create(this, "WebsiteDeployment")
    .destinationBucket(websiteBucket)
    .sources(Seq(Source.asset("../valheim-website")).asJava)
    .distribution(distribution)
    .build

  ARecord.Builder.create(this, "ARecord")
    .zone(hostedZone)
    .recordName(stageConfig.appDomain)
    .target(RecordTarget.fromAlias(new CloudFrontTarget(distribution)))
    .build
}
