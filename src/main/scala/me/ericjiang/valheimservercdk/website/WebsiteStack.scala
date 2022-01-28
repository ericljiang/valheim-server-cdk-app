package me.ericjiang.valheimservercdk.website

import me.ericjiang.valheimservercdk.StageConfig
import software.amazon.awscdk.services.certificatemanager.DnsValidatedCertificate
import software.amazon.awscdk.services.cloudfront.origins.S3Origin
import software.amazon.awscdk.services.cloudfront.{BehaviorOptions, Distribution}
import software.amazon.awscdk.services.route53.{HostedZone, HostedZoneAttributes}
import software.amazon.awscdk.services.s3.Bucket
import software.amazon.awscdk.services.s3.deployment.{BucketDeployment, Source}
import software.amazon.awscdk.{RemovalPolicy, Stack, StackProps}
import software.constructs.Construct

import scala.jdk.CollectionConverters._

class WebsiteStack(scope: Construct, id: String, props: StackProps = null)
  extends Stack(scope, id, props) {

  private val stageConfig: StageConfig = StageConfig.find(this)

  private val websiteBucket = Bucket.Builder.create(this, "WebsiteBucket")
    .websiteIndexDocument("index.html")
    .publicReadAccess(true)
    .removalPolicy(RemovalPolicy.DESTROY)
    .autoDeleteObjects(true)
    .build

  BucketDeployment.Builder.create(this, "WebsiteDeployment")
    .destinationBucket(websiteBucket)
    .sources(Seq(Source.asset("src/main/resources/website")).asJava)
    .build

  // TODO eliminate code duplication with ClientApi
  private val hostedZone = HostedZone.fromHostedZoneAttributes(this, "HostedZone", HostedZoneAttributes.builder
    .zoneName("ericjiang.me")
    .hostedZoneId("Z06067853SHQF3QW16T9N")
    .build)

  private val certificate = DnsValidatedCertificate.Builder.create(this, "Certificate")
    .domainName(stageConfig.appDomain)
    .hostedZone(hostedZone)
    .region("us-east-1")
    .build

  Distribution.Builder.create(this, "CloudFrontDistribution")
    .defaultBehavior(BehaviorOptions.builder
      .origin(new S3Origin(websiteBucket))
      .build)
    .domainNames(Seq(stageConfig.appDomain).asJava)
    .certificate(certificate)
    .build
}