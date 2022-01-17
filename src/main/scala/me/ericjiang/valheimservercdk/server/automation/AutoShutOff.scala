package me.ericjiang.valheimservercdk.server.automation

import me.ericjiang.valheimservercdk.server.compute.AutomatableGameServer
import software.constructs.Construct

class AutoShutOff(scope: Construct, id: String, server: AutomatableGameServer) extends Construct(scope, id) {
  server.idleAlarm.addAlarmAction()
}
