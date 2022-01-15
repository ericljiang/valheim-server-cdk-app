package me.ericjiang.valheimservercdk.server.compute

import software.amazon.awscdk.services.cloudwatch.Alarm
import software.amazon.awscdk.services.lambda.Function

/**
 * A game server that can be started and stopped by automation infrastructure.
 */
trait AutomatableGameServer {
  /**
   * Function that starts the server.
   */
  def startFunction: Function

  /**
   * Function that stops the server.
   */
  def stopFunction: Function

  /**
   * Function that retrieves the status of the server.
   *
   * At a minimum, status must include the information required for clients to connect to the server.
   */
  def statusFunction: Function

  /**
   * Alarm that indicates when the server is idle and can be shut down.
   */
  def idleAlarm: Alarm
}
