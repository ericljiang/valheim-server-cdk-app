package me.ericjiang.valheimservercdk

import me.ericjiang.valheimservercdk.pipeline.CdkPipelineStack
import software.amazon.awscdk.{App, StackProps}

object Main extends scala.App {
  val app = new App
  new CdkPipelineStack(app, "ValheimServerCdkPipeline", StackProps.builder
    .env(Environments.Default)
    .build)
  app.synth
}
