package dev.mongocamp.server.cli

import better.files.File
import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.cli.exception.NativeBuildException
import dev.mongocamp.server.cli.service.NativeImageBuildService
import dev.mongocamp.server.service.{ CoursierModuleService, PluginService }
import lukfor.progress.Components.{ SPINNER, TASK_NAME }
import lukfor.progress.TaskService
import lukfor.progress.tasks.ITaskRunnable
import lukfor.progress.tasks.monitors.ITaskMonitor
import picocli.CommandLine.Command
import picocli.CommandLine.Help.Ansi

import java.util.concurrent.Callable
@Command(
  name = "buildNative",
  description = Array("Build native Image from Server Instance")
)
class BuildNativeImageCommand extends Callable[Integer] with LazyLogging {

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
        try {
          val pluginUrls = PluginService.listOfReadableUrls().map(url => File(url))
          if (pluginUrls.nonEmpty) {
            val serverUrls = CoursierModuleService.loadServerWithAllDependencies()
            NativeImageBuildService.buildNativeImage(pluginUrls ++ serverUrls, "server-with-plugins")
          }
          else {
            println(Ansi.AUTO.string(s"@|red No image creation needed use default image|@"))
          }
        }
        catch {
          case e: Exception =>
            monitor.failed(e)
            response += 1
          case e: NativeBuildException =>
            monitor.failed(e)
            println(Ansi.AUTO.string(s"@|bold,underline,red Build Error:|@"))
            println(Ansi.AUTO.string(s"@|red ${e.buildMessage}|@"))
            response += 1
        }
      }
    }
    TaskService.monitor(SPINNER, TASK_NAME).run(prepareSystem, installNativeImage, buildNativeImage)
    response
  }

}
