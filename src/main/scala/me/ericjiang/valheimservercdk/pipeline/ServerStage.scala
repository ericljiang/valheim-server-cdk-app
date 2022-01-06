package me.ericjiang.valheimservercdk.pipeline

import me.ericjiang.valheimservercdk.server.ValheimServerStack
import software.amazon.awscdk.{Stage, StageProps}
import software.constructs.Construct

class ServerStage(scope: Construct, id: String, props: StageProps = null) extends Stage(scope, id, props) {
  new ValheimServerStack(this, "ServerStack")
}
