package me.ericjiang.valheimservercdk.server

import software.amazon.awscdk.services.lambda.{Function, InlineCode, Runtime}
import software.amazon.awscdk.{Stack, StackProps}
import software.constructs.Construct

class ValheimServerStack(scope: Construct, id: String, props: StackProps = null) extends Stack(scope, id, props) {
  Function.Builder.create(this, "TestLambdaFunction")
    .runtime(Runtime.NODEJS_14_X)
    .handler("index.handler")
    .code(new InlineCode("exports.handler = _ => 'Hello, CDK!';"))
    .build
}
