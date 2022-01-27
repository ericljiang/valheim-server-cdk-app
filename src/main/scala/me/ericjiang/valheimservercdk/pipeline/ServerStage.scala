package me.ericjiang.valheimservercdk.pipeline

import me.ericjiang.valheimservercdk.server.AutomatedServerStack
import me.ericjiang.valheimservercdk.website.WebsiteStack
import software.amazon.awscdk.{Stage, StageProps}
import software.constructs.Construct

import scala.concurrent.duration.Duration

class ServerStage(scope: Construct, id: String, props: StageProps = null, idleDuration: Duration)
  extends Stage(scope, id, props) {
  val serverStack = new AutomatedServerStack(this, "ServerStack", idleDuration = idleDuration)
  val websiteStack = new WebsiteStack(this, "WebsiteStack", apiEndpoint = serverStack.apiEndpoint)
  websiteStack.addDependency(serverStack)
}
