package me.ericjiang.valheimservercdk.website

import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.services.s3.deployment.{BucketDeployment, Source}
import software.amazon.awscdk.services.s3.{Bucket, IBucket}
import software.constructs.Construct

import scala.jdk.CollectionConverters._

class Website(scope: Construct, id: String) extends Construct(scope, id) {

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
