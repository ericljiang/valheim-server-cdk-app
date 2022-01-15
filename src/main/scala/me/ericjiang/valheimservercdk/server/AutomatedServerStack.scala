package me.ericjiang.valheimservercdk.server

import me.ericjiang.valheimservercdk.server.automation.api.ClientApi
import me.ericjiang.valheimservercdk.server.compute.ValheimEc2Instance
import software.amazon.awscdk.{Stack, StackProps}
import software.constructs.Construct

class AutomatedServerStack(scope: Construct, id: String, props: StackProps = null) extends Stack(scope, id, props) {
  new ClientApi(this, "api")
  new ValheimEc2Instance(this, "compute")
}
