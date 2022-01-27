package me.ericjiang.valheimservercdk.website

import software.amazon.awscdk.{Stack, StackProps}
import software.constructs.Construct

class WebsiteStack(scope: Construct, id: String, props: StackProps = null, apiEndpoint: String)
  extends Stack(scope, id, props) {

}
