package me.ericjiang.valheimservercdk.pipeline

import me.ericjiang.valheimservercdk.Environments
import software.amazon.awscdk.pipelines.{CodePipeline, CodePipelineSource, ConnectionSourceOptions, ManualApprovalStep, ShellStep}
import software.amazon.awscdk.services.codestarconnections.CfnConnection
import software.amazon.awscdk.{Stack, StackProps, StageProps}
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

  // https://docs.aws.amazon.com/cdk/v2/guide/cdk_pipeline.html
  private val pipeline = CodePipeline.Builder.create(this, "pipeline")
    .synth(ShellStep.Builder.create("Synth")
      .input(CodePipelineSource.connection("ericljiang/valheim-server-cdk-app", "main", ConnectionSourceOptions.builder
        .connectionArn(codeStarConnection.getAttrConnectionArn)
        .build))
      .commands(Seq("npm install -g aws-cdk", "cdk synth").asJava)
      .build)
    .build

  pipeline.addStage(new ServerStage(this, "ValheimServerBeta", StageProps.builder.env(Environments.Default).build))
  pipeline.addStage(new ServerStage(this, "ValheimServerProd", StageProps.builder.env(Environments.Default).build))
    .addPre(new ManualApprovalStep("PromoteToProd"))
}
