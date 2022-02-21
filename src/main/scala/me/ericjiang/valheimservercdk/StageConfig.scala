package me.ericjiang.valheimservercdk

import software.amazon.awscdk.{Environment, Stage}
import software.constructs.Construct

import scala.concurrent.duration.{Duration, DurationInt}

trait StageConfig {
  def environment: Environment
  def stageName: String
  def appDomain: String
  def gameServerDomain: String
  def apiPath: String
  def idleDuration: Duration
  def logGroup: String = stageName
  def metricNamespace: String = stageName
  def rollbackInstanceOnFailure: Boolean = true
}

object StageConfig {
  case object Beta extends StageConfig {
    override def environment: Environment = Environments.Default
    override def stageName: String = "ValheimServerBeta"
    override def appDomain: String = "valheim-beta.ericjiang.me"
    override def gameServerDomain: String = "vh-beta.ericjiang.me"
    override def apiPath: String = "api"
    override def idleDuration: Duration = 10.minutes
  }

  case object Prod extends StageConfig {
    override def environment: Environment = Environments.Default
    override def stageName: String = "ValheimServerProd"
    override def appDomain: String = "valheim.ericjiang.me"
    override def gameServerDomain: String = "vh.ericjiang.me"
    override def apiPath: String = "api"
    override def idleDuration: Duration = 1.hour
  }

  val mapping: Map[String, StageConfig] = Seq(Beta, Prod).map(config => config.stageName -> config).toMap
  def find(stageName: String): StageConfig = mapping(stageName)
  def find(construct: Construct): StageConfig = find(Stage.of(construct).getStageName)
}
