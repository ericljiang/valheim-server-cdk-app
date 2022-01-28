package me.ericjiang.valheimservercdk.pipeline

import me.ericjiang.valheimservercdk.StageConfig
import me.ericjiang.valheimservercdk.server.AutomatedServerStack
import me.ericjiang.valheimservercdk.website.WebsiteStack
import software.amazon.awscdk.{Stage, StageProps}
import software.constructs.Construct

class ServerStage(scope: Construct, stageConfig: StageConfig)
  extends Stage(scope, stageConfig.stageName, StageProps.builder.env(stageConfig.environment).build) {
  val serverStack = new AutomatedServerStack(this, "ServerStack", idleDuration = stageConfig.idleDuration)
  val websiteStack = new WebsiteStack(this, "WebsiteStack")
  websiteStack.addDependency(serverStack)
}
