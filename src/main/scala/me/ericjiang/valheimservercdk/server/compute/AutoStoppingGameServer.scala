package me.ericjiang.valheimservercdk.server.compute

import software.amazon.awscdk.services.lambda.Function

/**
 * A game server that can be started and stopped by automation infrastructure.
 */
trait AutoStoppingGameServer {
  /**
   * Function that starts the server.
   */
  def startFunction: Function

  /**
   * Function that retrieves the status of the server.
   *
   * At a minimum, status must include the information required for clients to connect to the server.
   */
  def statusFunction: Function
}
