package dev.mongocamp.server.cli.prepare

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.service.CoursierModuleService
import lukfor.progress.Components.{ SPINNER, TASK_NAME }
import lukfor.progress.TaskService
import lukfor.progress.tasks.ITaskRunnable
import lukfor.progress.tasks.monitors.ITaskMonitor
import picocli.CommandLine.Command
import picocli.CommandLine.Help.Ansi

import java.util.concurrent.Callable
@Command(
  name = "cache",
  description = Array("Download Server Jars to local Server")
)
class CacheCommand extends Callable[Integer] with LazyLogging {

  def call(): Integer = {
    var response = 0
    val task = new ITaskRunnable() {
      def run(monitor: ITaskMonitor): Unit = {
        monitor.begin("Download Server Jars from Maven Central")
        try {
          val files = CoursierModuleService.loadServerWithAllDependencies()
          monitor.update(s"${files.size} Jars from Maven Central downloaded")
        }
        catch {
          case e: Exception =>
            monitor.failed(e)
            println(Ansi.AUTO.string(s"@|bold,underline,red Build Error:|@"))
            println(Ansi.AUTO.string(s"@|red ${e.getMessage}|@"))
            response += 1
        }
      }
    }
    TaskService.monitor(SPINNER, TASK_NAME).run(task)
    response
  }

}
