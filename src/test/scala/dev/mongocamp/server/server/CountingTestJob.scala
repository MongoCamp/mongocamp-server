package dev.mongocamp.server.server

import dev.mongocamp.server.server.CountingTestJob.counter
import org.quartz.{Job, JobExecutionContext}

class CountingTestJob extends Job {

  override def execute(context: JobExecutionContext): Unit = {
    counter += 1
    context
  }

}

object CountingTestJob {
  var counter = 0
}
