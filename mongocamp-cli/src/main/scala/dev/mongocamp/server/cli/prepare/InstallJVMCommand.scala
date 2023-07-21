package dev.mongocamp.server.cli.prepare

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.cli.service.JvmService
import lukfor.progress.Components.{SPINNER, TASK_NAME}
import lukfor.progress.TaskService
import lukfor.progress.tasks.ITaskRunnable
import lukfor.progress.tasks.monitors.ITaskMonitor
import picocli.CommandLine.Command
import picocli.CommandLine.Help.Ansi

@Command(
  name = "jvm",
  description = Array("Check or Install GraalVM for build and run Commands")
)
class InstallJVMCommand extends Runnable with LazyLogging {

  def run(): Unit = {
    val task = new ITaskRunnable() {
      def run(monitor: ITaskMonitor): Unit = {
        monitor.begin("Install JVM")
        try {
          monitor.update(s"Java Home: ${JvmService.javaHome}")
        } catch {
          case e: Exception =>
            monitor.failed(e)
            println(Ansi.AUTO.string(s"@|bold,underline,red Build Error:|@"))
            println(Ansi.AUTO.string(s"@|red ${e.getMessage}|@"))
        }
      }
    }
    TaskService.monitor(SPINNER, TASK_NAME).run(task)
  }

}
