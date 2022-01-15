package me.ericjiang.valheimservercdk.pipeline

import me.ericjiang.valheimservercdk.server.AutomatedServerStack
import software.amazon.awscdk.{Stage, StageProps}
import software.constructs.Construct

class ServerStage(scope: Construct, id: String, props: StageProps = null) extends Stage(scope, id, props) {
  new AutomatedServerStack(this, "ServerStack")
}
