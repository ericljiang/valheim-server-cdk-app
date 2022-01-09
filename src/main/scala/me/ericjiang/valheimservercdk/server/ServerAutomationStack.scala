package me.ericjiang.valheimservercdk.server

import me.ericjiang.valheimservercdk.server.automation.api.ClientApi
import me.ericjiang.valheimservercdk.server.compute.AutomatableCompute
import software.amazon.awscdk.{Stack, StackProps}
import software.constructs.Construct

class ServerAutomationStack(scope: Construct, id: String, props: StackProps = null) extends Stack(scope, id, props) {
  new ClientApi(this, "api")
  new AutomatableCompute(this, "compute")
}
