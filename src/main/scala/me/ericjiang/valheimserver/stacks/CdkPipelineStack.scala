package me.ericjiang.valheimserver.stacks

import software.amazon.awscdk.pipelines.{CodePipeline, CodePipelineSource, ShellStep}
import software.amazon.awscdk.{Stack, StackProps}
import software.constructs.Construct

import scala.jdk.CollectionConverters._

class CdkPipelineStack(scope: Construct, id: String, props: StackProps = null) extends Stack(scope, id, props) {
  private val pipeline = CodePipeline.Builder
    .create(this, "pipeline")
    .pipelineName("CdkPipeline")
    .synth(ShellStep.Builder
      .create("Synth")
      .input(CodePipelineSource.gitHub("ericljiang/todo", "main"))
      .commands(Seq("npm install -g aws-cdk", "cdk synth").asJava)
      .build)
    .build
}
