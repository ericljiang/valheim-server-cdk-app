package me.ericjiang.valheimservercdk.util

import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.ec2.Instance

object CdkUtils {
  /** Extension methods for [[software.amazon.awscdk.services.ec2.Instance]]. */
  implicit class InstanceExtensions(val instance: Instance) {
    /** ARN in the format of "arn:$partition:ec2:*:$account_id:instance/$instance_id" */
    def getArn: String = {
      val stack = Stack.of(instance)
      s"arn:${stack.getPartition}:ec2:${stack.getRegion}:${stack.getAccount}:instance/${instance.getInstanceId}"
    }
  }
}
