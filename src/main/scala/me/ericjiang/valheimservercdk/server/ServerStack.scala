package me.ericjiang.valheimservercdk.server

import me.ericjiang.valheimservercdk.network.DnsRouting
import me.ericjiang.valheimservercdk.server.api.ClientApi
import me.ericjiang.valheimservercdk.server.compute.AutomaticShutdown
import me.ericjiang.valheimservercdk.server.valheim.AutomatableValheimServer
import me.ericjiang.valheimservercdk.website.Website
import software.amazon.awscdk.{Stack, StackProps}
import software.constructs.Construct

import scala.concurrent.duration.Duration

class ServerStack(scope: Construct, id: String, props: StackProps = null, idleDuration: Duration)
  extends Stack(scope, id, props) {

  private val server = new AutomatableValheimServer(this, "Server", idleDuration)
  new AutomaticShutdown(this, "AutomaticShutdown", server)

  private val api = new ClientApi(this, "Api", server)

  val website = new Website(this, "WebsiteStack")

  val dnsRouting = new DnsRouting(this, "NetworkStack",
    websiteBucket = website.bucket,
    apiDomain = api.getDomain,
    gameServer = server)
}
