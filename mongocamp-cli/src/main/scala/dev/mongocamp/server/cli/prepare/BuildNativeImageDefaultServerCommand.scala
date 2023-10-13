package dev.mongocamp.server.cli.prepare

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.cli.exception.NativeBuildException
import dev.mongocamp.server.cli.service.NativeImageBuildService
import dev.mongocamp.server.service.CoursierModuleService
import lukfor.progress.Components.{SPINNER, TASK_NAME}
import lukfor.progress.TaskService
import lukfor.progress.tasks.ITaskRunnable
import lukfor.progress.tasks.monitors.ITaskMonitor
import picocli.CommandLine.Command
import picocli.CommandLine.Help.Ansi

import java.util.concurrent.Callable
@Command(
  name = "native",
  description = Array("Build native Image from Server Instance")
)
class BuildNativeImageDefaultServerCommand extends Callable[Integer] with LazyLogging {

  def call(): Integer = {
    var response = 0
    val prepareSystem = new ITaskRunnable() {
      def run(monitor: ITaskMonitor): Unit = {
        monitor.begin("Prepare System for using Build")
        try
          NativeImageBuildService.prepareSystemForBuildingNativeImage()
        catch {
          case e: Exception =>
            monitor.failed(e)
            response += 1
        }
      }
    }
    val installNativeImage = new ITaskRunnable() {
      def run(monitor: ITaskMonitor): Unit = {
        monitor.begin("Install Native Image")
        try
          NativeImageBuildService.installNativeImageExecutable()
        catch {
          case e: Exception =>
            monitor.failed(e)
            response += 1
        }
      }
    }
    val buildNativeImage = new ITaskRunnable() {
      def run(monitor: ITaskMonitor): Unit = {
        monitor.begin("Building Native Image")
        try
          NativeImageBuildService.buildNativeImage(CoursierModuleService.loadServerWithAllDependencies(), "server-raw")
        catch {
          case e: Exception =>
            monitor.failed(e)
            println(Ansi.AUTO.string(s"@|bold,underline,red Build Error:|@"))
            println(Ansi.AUTO.string(s"@|red ${e.getMessage}|@"))
            response += 1
          case e: NativeBuildException =>
            monitor.failed(e)
            println(Ansi.AUTO.string(s"@|bold,underline,red Native Image Build Error:|@"))
            println(Ansi.AUTO.string(s"@|red ${e.buildMessage}|@"))
            response += 1
        }
      }
    }
    TaskService.monitor(SPINNER, TASK_NAME).run(prepareSystem, installNativeImage, buildNativeImage)
    response
  }

}
