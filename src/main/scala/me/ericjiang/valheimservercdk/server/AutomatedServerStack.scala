package me.ericjiang.valheimservercdk.server

import me.ericjiang.valheimservercdk.server.api.ClientApi
import me.ericjiang.valheimservercdk.server.compute.{AutomatableGameServer, AutomaticShutdown}
import me.ericjiang.valheimservercdk.server.valheim.AutomatableValheimServer
import software.amazon.awscdk.{Stack, StackProps}
import software.constructs.Construct

import scala.concurrent.duration.Duration

class AutomatedServerStack(scope: Construct, id: String, props: StackProps = null, idleDuration: Duration)
  extends Stack(scope, id, props) {

  val server: AutomatableGameServer = new AutomatableValheimServer(this, "Server", idleDuration)
  private val api = new ClientApi(this, "Api", server)
  new AutomaticShutdown(this, "AutomaticShutdown", server)

  def apiDomain: String = api.getDomain
}
