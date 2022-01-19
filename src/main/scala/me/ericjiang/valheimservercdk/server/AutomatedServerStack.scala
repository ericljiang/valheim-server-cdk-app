package me.ericjiang.valheimservercdk.server

import me.ericjiang.valheimservercdk.server.automation.api.ClientApi
import me.ericjiang.valheimservercdk.server.compute.{AutoStoppingGameServer, AutoStoppingValheimServer}
import software.amazon.awscdk.{Stack, StackProps}
import software.constructs.Construct

import scala.concurrent.duration.Duration

class AutomatedServerStack(scope: Construct, id: String, props: StackProps = null, idleDuration: Duration)
  extends Stack(scope, id, props) {
  new ClientApi(this, "api")
  val server: AutoStoppingGameServer = new AutoStoppingValheimServer(this, "Server", idleDuration)
}
