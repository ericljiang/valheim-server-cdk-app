package me.ericjiang.valheimservercdk.server

import me.ericjiang.valheimservercdk.server.api.ClientApi
import software.amazon.awscdk.{Stack, StackProps}
import software.constructs.Construct

class ValheimServerStack(scope: Construct, id: String, props: StackProps = null) extends Stack(scope, id, props) {
  new ClientApi(this, "api")
}
