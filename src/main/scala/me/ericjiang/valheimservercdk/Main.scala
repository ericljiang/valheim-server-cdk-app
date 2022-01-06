package me.ericjiang.valheimservercdk

import me.ericjiang.valheimservercdk.pipeline.CdkPipelineStack
import software.amazon.awscdk.{App, Environment, StackProps}

object Main extends scala.App {
  val app = new App
  new CdkPipelineStack(app, "CdkPipelineStack", StackProps.builder
    .env(Environment.builder.account("323729054419").region("us-west-1").build)
    .build)
  app.synth
}
