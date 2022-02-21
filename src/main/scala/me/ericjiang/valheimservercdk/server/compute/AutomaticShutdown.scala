package me.ericjiang.valheimservercdk.server.compute

import software.amazon.awscdk.services.cloudwatch.actions.SnsAction
import software.amazon.awscdk.services.cloudwatch.{Alarm, AlarmRule, ComparisonOperator, CompositeAlarm}
import software.amazon.awscdk.services.sns.Topic
import software.amazon.awscdk.services.sns.subscriptions.LambdaSubscription
import software.constructs.Construct

class AutomaticShutdown(scope: Construct, id: String, autoStoppingGameServer: AutomatableGameServer)
  extends Construct(scope, id) {

  private val recentlyStartedAlarm = Alarm.Builder.create(this, "RecentlyStarted")
    .alarmDescription("Ensures that the idle alarm will enter OK state for at least one period after server starts.")
    .metric(autoStoppingGameServer.uptimeMetric)
    .comparisonOperator(ComparisonOperator.LESS_THAN_OR_EQUAL_TO_THRESHOLD)
    .threshold(autoStoppingGameServer.uptimeMetric.getPeriod.toSeconds)
    .evaluationPeriods(1)
    .build

  private val idleNotifications = new Topic(this, "IdleNotifications")
  CompositeAlarm.Builder.create(this, "AutoShutdown")
    .alarmRule(AlarmRule.allOf(
      AlarmRule.not(recentlyStartedAlarm),
      autoStoppingGameServer.idleAlarm
    ))
    .build
    .addAlarmAction(new SnsAction(idleNotifications))
  idleNotifications.addSubscription(LambdaSubscription.Builder.create(autoStoppingGameServer.stopFunction).build)
}
