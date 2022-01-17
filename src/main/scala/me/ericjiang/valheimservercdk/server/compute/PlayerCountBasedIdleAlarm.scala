package me.ericjiang.valheimservercdk.server.compute

import software.amazon.awscdk.services.cloudwatch.{Alarm, ComparisonOperator, Metric}
import software.constructs.Construct

class PlayerCountBasedIdleAlarm(scope: Construct, id: String, playerCountMetric: Metric) extends Construct(scope, id) {
  val alarm: Alarm = Alarm.Builder.create(this, "IdleAlarm")
    .alarmDescription("Indicates that the server is idle and can be shut down.")
    .metric(playerCountMetric)
    .comparisonOperator(ComparisonOperator.LESS_THAN_OR_EQUAL_TO_THRESHOLD)
    .threshold(0)
    .evaluationPeriods(12) // TODO add param for idle duration and calculate this based on metric period
    .build
}
