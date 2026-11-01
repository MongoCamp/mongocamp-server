package dev.mongocamp.server.cli.prepare

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.cli.service.JvmService
import java.util.concurrent.Callable
import lukfor.progress.tasks.monitors.ITaskMonitor
import lukfor.progress.tasks.ITaskRunnable
import lukfor.progress.Components.SPINNER
import lukfor.progress.Components.TASK_NAME
import lukfor.progress.TaskService
import picocli.CommandLine.Command
import picocli.CommandLine.Help.Ansi
@Command(name = "jvm", description = Array("Check or Install GraalVM for build and run Commands"))
class InstallJVMCommand extends Callable[Integer] with LazyLogging {

  def call(): Integer = {
    var response = 0
    val task = new ITaskRunnable() {
      def run(monitor: ITaskMonitor): Unit = {
        monitor.begin("Install JVM")
        try
          monitor.update(s"Java Home: ${JvmService.javaHome}")
        catch {
          case e: Exception =>
            monitor.failed(e)
            println(Ansi.AUTO.string(s"@|bold,underline,red Build Error:|@"))
            println(Ansi.AUTO.string(s"@|red ${e.getMessage}|@"))
            response = 1
        }
      }
    }
    TaskService.monitor(SPINNER, TASK_NAME).run(task)
    response
  }

}
