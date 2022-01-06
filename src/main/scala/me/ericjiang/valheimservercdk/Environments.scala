package me.ericjiang.valheimservercdk

import software.amazon.awscdk.Environment

object Environments {
  val Default: Environment = Environment.builder.account("323729054419").region("us-west-1").build
}
