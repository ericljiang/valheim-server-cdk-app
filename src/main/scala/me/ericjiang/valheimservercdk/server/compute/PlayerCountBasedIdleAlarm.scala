package me.ericjiang.valheimservercdk.server.compute

import software.amazon.awscdk.services.cloudwatch.{Alarm, ComparisonOperator, Metric, TreatMissingData}
import software.constructs.Construct

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

class PlayerCountBasedIdleAlarm(scope: Construct, id: String, playerCountMetric: Metric, idleDuration: Duration)
  extends Construct(scope, id) {

  private val evaluationPeriods =
    (idleDuration / Duration(playerCountMetric.getPeriod.toSeconds.longValue, TimeUnit.SECONDS)).ceil

  val alarm: Alarm = Alarm.Builder.create(this, "IdleAlarm")
    .alarmDescription("Indicates that the server is idle and can be shut down.")
    .metric(playerCountMetric)
    .comparisonOperator(ComparisonOperator.LESS_THAN_OR_EQUAL_TO_THRESHOLD)
    .threshold(0)
    .evaluationPeriods(evaluationPeriods)
    .treatMissingData(TreatMissingData.NOT_BREACHING)
    .build
}
