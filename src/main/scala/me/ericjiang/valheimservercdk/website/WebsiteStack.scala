package me.ericjiang.valheimservercdk.website

import software.amazon.awscdk.services.cloudfront.origins.S3Origin
import software.amazon.awscdk.services.cloudfront.{BehaviorOptions, Distribution}
import software.amazon.awscdk.services.s3.Bucket
import software.amazon.awscdk.services.s3.deployment.{BucketDeployment, Source}
import software.amazon.awscdk.{RemovalPolicy, Stack, StackProps}
import software.constructs.Construct

import scala.jdk.CollectionConverters._

class WebsiteStack(scope: Construct, id: String, props: StackProps = null)
  extends Stack(scope, id, props) {
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

  Distribution.Builder.create(this, "CloudFrontDistribution")
    .defaultBehavior(BehaviorOptions.builder
      .origin(new S3Origin(websiteBucket))
      .build)
    .build
}
