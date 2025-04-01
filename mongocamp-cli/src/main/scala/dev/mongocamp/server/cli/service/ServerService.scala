package dev.mongocamp.server.cli.service

import better.files.{ File, FileMonitor }
import dev.mongocamp.server.cli.Main
import dev.mongocamp.server.library.BuildInfo
import dev.mongocamp.server.service.PluginService
import io.circe.parser._
import picocli.CommandLine.Help.Ansi

import java.nio.file.{ Path, StandardWatchEventKinds => EventType, WatchEvent }
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.sys.process
import scala.sys.process.Process

object ServerService {
  private var processOption: Option[process.Process] = None
  private var monitoredSystemMode: String            = "default"
  val eventDirectory                                 = File(File.temp.pathAsString + s"/mongocamp_${BuildInfo.version}")

  monitorRestart()

  def monitorRestart(): Unit = {
    if (!eventDirectory.exists) {
      eventDirectory.createDirectories()
    }
    val watcher = new FileMonitor(eventDirectory, recursive = false) {
      override def onEvent(eventType: WatchEvent.Kind[Path], file: File, count: Int) = eventType match {
        case EventType.ENTRY_CREATE =>
          val jsonString         = file.contentAsString
          val jsonMapParseResult = decode[Map[String, String]](jsonString)
          val jsonMap            = jsonMapParseResult.getOrElse(throw new Exception(s"Could not parse file ${file.toString()}"))
          while (System.currentTimeMillis() < jsonMap("timestamp").toLong)
            "wait"
          bootServer(monitoredSystemMode)
        case EventType.ENTRY_MODIFY =>
        case EventType.ENTRY_DELETE =>
      }
    }
    watcher.start()(ExecutionContext.global)
  }

  def startServer(mode: String): Int = {
    monitoredSystemMode = mode
    println(Ansi.AUTO.string(s"@|yellow Start MongoCamp Server with $mode|@"))
    bootServer(mode)

    while (processOption.exists(_.isAlive()) || eventDirectory.children.nonEmpty)
      Thread.sleep(2.seconds.toMillis)

    processOption match {
      case Some(process) =>
        process.exitValue()
      case None =>
        0
    }
  }

  private def bootServer(mode: String): Unit = {
    mode match {
      case s: String if s.equalsIgnoreCase("jvm") =>
        processOption = Some(JvmStartService.startServer())

      case s: String if s.equalsIgnoreCase("native") =>
        val pluginUrls = PluginService
          .listOfReadableUrls()
          .map(
            url => File(url)
          )
        if (pluginUrls.nonEmpty) {
          Main.commandLine.execute(List("buildNative"): _*)
          processOption = Some(ProcessExecutorService.stoutProcessBuilder("./server-with-plugins"))
        }
        else {
          processOption = Some(ProcessExecutorService.stoutProcessBuilder("./server-raw"))
        }

      case s: String if s.equalsIgnoreCase("default") =>
        val pluginUrls = PluginService
          .listOfReadableUrls()
          .map(
            url => File(url)
          )

        if (pluginUrls.nonEmpty) {
          processOption = Some(JvmStartService.startServer())
        }
        else {
          processOption = Some(ProcessExecutorService.stoutProcessBuilder("./server-raw"))
        }

      case _ =>
        println(Ansi.AUTO.string(s"@|bold,red Could not found definition for Mode $mode|@"))
    }
    eventDirectory.children.foreach(_.delete())
  }

  def stopServer(): Unit = {
    processOption match {
      case Some(process) =>
        process.destroy()
      case None =>
    }
  }

  def isRunning: Boolean = processOption.nonEmpty

  def getProcessId(process: Process): Option[Int] = {
    val procField = process.getClass.getDeclaredField("p")
    procField.setAccessible(true)
    val proc = procField.get(process)
    procField.setAccessible(false)
    "pid=(.*?),".r.findFirstIn(proc.toString).flatMap(_.replace("pid=", "").replace(",", "").toIntOption)
  }

}
