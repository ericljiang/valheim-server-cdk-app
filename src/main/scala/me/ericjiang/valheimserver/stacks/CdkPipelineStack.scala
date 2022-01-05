package me.ericjiang.valheimserver.stacks

import software.amazon.awscdk.pipelines.{CodePipeline, CodePipelineSource, ConnectionSourceOptions, ShellStep}
import software.amazon.awscdk.services.codestarconnections.CfnConnection
import software.amazon.awscdk.{Stack, StackProps}
import software.constructs.Construct

import scala.jdk.CollectionConverters._

class CdkPipelineStack(scope: Construct, id: String, props: StackProps = null) extends Stack(scope, id, props) {

  // https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-codestarconnections-connection.html
  private val codeStarConnection = CfnConnection.Builder.create(this, "CodeStarConnection")
    .connectionName("GitHubConnectionEricljiang")
    .providerType("GitHub")
    .build
  // https://docs.aws.amazon.com/cdk/v2/guide/cdk_pipeline.html
  private val pipeline = CodePipeline.Builder
    .create(this, "pipeline")
    .pipelineName("CdkPipeline")
    .synth(ShellStep.Builder
      .create("Synth")
      .input(CodePipelineSource.connection("ericljiang/valheim-server-cdk-app", "main", ConnectionSourceOptions.builder
        .connectionArn(codeStarConnection.getAttrConnectionArn)
        .build))
      .commands(Seq("npm install -g aws-cdk", "cdk synth").asJava)
      .build)
    .build
}
