package dev.mongocamp.server.test

import dev.mongocamp.server.test.CountingTestJob.counter
import org.quartz.{ Job, JobExecutionContext }
class CountingTestJob extends Job {

  override def execute(context: JobExecutionContext): Unit = {
    counter += 1
    context
  }

}

object CountingTestJob {
  var counter = 0
}
