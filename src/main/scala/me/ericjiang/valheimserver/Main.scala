package me.ericjiang.valheimserver

object Main extends App {
  println("Hello, world!")
  val app = new ValheimServerCdkApp("323729054419", "us-west-1")
  app.synth
}
