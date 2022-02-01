package me.ericjiang.valheimservercdk

import software.amazon.awscdk.services.route53.{HostedZone, HostedZoneAttributes, IHostedZone}
import software.amazon.awscdk.{Stack, StackProps}
import software.constructs.Construct

class CommonStack(scope: Construct, id: String, props: StackProps = null) extends Stack(scope, id, props) {
  val hostedZone: IHostedZone = HostedZone.fromHostedZoneAttributes(this, "HostedZone", HostedZoneAttributes.builder
    .zoneName("ericjiang.me")
    .hostedZoneId("Z06067853SHQF3QW16T9N")
    .build)
}
