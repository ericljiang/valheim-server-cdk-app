package me.ericjiang.valheimservercdk.server.compute

import software.amazon.awscdk.services.cloudwatch.{Alarm, Metric}
import software.amazon.awscdk.services.events.EventPattern
import software.amazon.awscdk.services.lambda.Function

/**
 * A game server that can be started on demand and stops automatically.
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

  def startEventPattern: EventPattern

  def idleAlarm: Alarm

  def uptimeMetric: Metric
}
