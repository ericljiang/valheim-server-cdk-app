package me.ericjiang.valheimservercdk.website

import software.amazon.awscdk.services.s3.deployment.{BucketDeployment, Source}
import software.amazon.awscdk.services.s3.{Bucket, IBucket}
import software.amazon.awscdk.{RemovalPolicy, Stack, StackProps}
import software.constructs.Construct

import scala.jdk.CollectionConverters._

class WebsiteStack(scope: Construct, id: String, props: StackProps = null) extends Stack(scope, id, props) {

  val bucket: IBucket = Bucket.Builder.create(this, "WebsiteBucket")
    .websiteIndexDocument("index.html")
    .publicReadAccess(true)
    .removalPolicy(RemovalPolicy.DESTROY)
    .autoDeleteObjects(true)
    .build

  BucketDeployment.Builder.create(this, "WebsiteDeployment")
    .destinationBucket(bucket)
    .sources(Seq(Source.asset("../valheim-website")).asJava)
    .build
}
