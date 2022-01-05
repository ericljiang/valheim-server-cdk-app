package me.ericjiang.valheimserver

import me.ericjiang.valheimserver.stacks.CdkPipelineStack
import software.amazon.awscdk.{App, Environment, StackProps}

class ValheimServerCdkApp(accountId: String, region: String) extends App {
  new CdkPipelineStack(this, "CdkPipelineStack", StackProps.builder
    .env(Environment.builder.account(accountId).region(region).build)
    .build)
}
