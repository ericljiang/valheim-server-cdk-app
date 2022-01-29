package me.ericjiang.valheimservercdk.pipeline

import me.ericjiang.valheimservercdk.StageConfig
import software.amazon.awscdk.pipelines._
import software.amazon.awscdk.services.codestarconnections.CfnConnection
import software.amazon.awscdk.{Stack, StackProps}
import software.constructs.Construct

import scala.jdk.CollectionConverters._

/**
 * Self-mutating pipeline that deploys the CDK app.
 */
class CdkPipelineStack(scope: Construct, id: String, props: StackProps = null) extends Stack(scope, id, props) {
  // The connection is in PENDING state when created through CFN and needs to updated in the console.
  // https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-codestarconnections-connection.html
  private val codeStarConnection = CfnConnection.Builder.create(this, "CodeStarConnection")
    .connectionName("GitHubConnectionEricljiang")
    .providerType("GitHub")
    .build

  private val websiteBuild = ShellStep.Builder.create("WebsiteBuild")
    .input(gitHubSource("valheim-website"))
    .primaryOutputDirectory("./build")
    .commands(Seq(
      "npm ci",
      "npm run build"
    ).asJava)
    .build

  // https://docs.aws.amazon.com/cdk/v2/guide/cdk_pipeline.html
  private val pipeline = CodePipeline.Builder.create(this, "pipeline")
    .synth(ShellStep.Builder.create("Synth")
      .input(gitHubSource("valheim-server-cdk-app"))
      .additionalInputs(Map(
        "../valheim-website" -> websiteBuild
      ).asJava)
      .commands(Seq(
        "npm install -g aws-cdk",
        "cdk synth"
      ).asJava)
      .build)
    .build

  private val beta = pipeline.addStage(new ServerStage(this, StageConfig.Beta))
  private val prod = pipeline.addStage(new ServerStage(this, StageConfig.Prod))
  prod.addPre(new ManualApprovalStep("PromoteToProd"))

  def gitHubSource(repository: String, branch: String = "main"): CodePipelineSource =
    CodePipelineSource.connection(s"ericljiang/$repository", branch, ConnectionSourceOptions.builder
      .connectionArn(codeStarConnection.getAttrConnectionArn)
      .build)
}
