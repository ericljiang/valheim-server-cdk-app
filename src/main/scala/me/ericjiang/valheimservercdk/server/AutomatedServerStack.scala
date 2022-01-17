package me.ericjiang.valheimservercdk.server

import me.ericjiang.valheimservercdk.server.automation.AutoShutOff
import me.ericjiang.valheimservercdk.server.automation.api.ClientApi
import me.ericjiang.valheimservercdk.server.compute.{AutomatableGameServer, AutomatableValheimServer}
import software.amazon.awscdk.{Stack, StackProps}
import software.constructs.Construct

class AutomatedServerStack(scope: Construct, id: String, props: StackProps = null) extends Stack(scope, id, props) {
  new ClientApi(this, "api")
  val server: AutomatableGameServer = new AutomatableValheimServer(this, "Server")
  new AutoShutOff(this, "AutoShutOff", server)
}
