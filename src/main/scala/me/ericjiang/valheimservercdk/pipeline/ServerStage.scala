package me.ericjiang.valheimservercdk.pipeline

import me.ericjiang.valheimservercdk.{CommonStack, StageConfig}
import me.ericjiang.valheimservercdk.server.AutomatedServerStack
import me.ericjiang.valheimservercdk.website.WebsiteStack
import software.amazon.awscdk.{Stage, StageProps}
import software.constructs.Construct

class ServerStage(scope: Construct, stageConfig: StageConfig)
  extends Stage(scope, stageConfig.stageName, StageProps.builder.env(stageConfig.environment).build) {
  val commonStack = new CommonStack(this, "CommonStack")
  val serverStack = new AutomatedServerStack(this, "ServerStack",
    idleDuration = stageConfig.idleDuration,
    hostedZone = commonStack.hostedZone)
  val websiteStack = new WebsiteStack(this, "WebsiteStack",
    hostedZone = commonStack.hostedZone)
}
