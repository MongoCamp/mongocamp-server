package dev.mongocamp.server.cli.service

import com.vdurmont.semver4j.Semver
import coursier.jvm.{JavaHome, JvmCache}
import dev.mongocamp.server.cli.BuildInfo

import java.io.File
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext}

object JvmService {

  private lazy val jvmCache = new JvmCache().withIndex("https://raw.githubusercontent.com/coursier/jvm-index/master/index.json")

  private lazy val buildJavaVersion = new Semver(BuildInfo.buildJavaVersion)

  lazy val javaHome: String = {
    val systemValue = System.getProperty("java.home")
    val envValue = System.getenv("JAVA_HOME")
    val preferredJavaHome: String = if (systemValue != null) {
      systemValue
    }
    else if (envValue != null) {
      envValue
    }
    else {
      val installedJvm = installJvm()
      installedJvm.toString
    }
    checkJvmForIsGraalVmOrInstall(preferredJavaHome)
  }

  private def installJvm(): File = {
    val csJavaHome = new JavaHome().withCache(jvmCache)
    val fileFuture  = csJavaHome.get(findBestJavaVersionToDownload())
    val file = Await.result(fileFuture.future()(ExecutionContext.global), 5.minutes)
    file
  }

  private def findBestJavaVersionToDownload(): String = {
    val availableVersions = Await.result(jvmCache.index.get.future()(ExecutionContext.global), 5.minutes).available().getOrElse(Map())
    val graalvmKey       = s"graalvm-java${buildJavaVersion.getMajor}"
    val graalVersions    = availableVersions.get(s"$graalvmKey").getOrElse(throw new Exception("no possible java version for installation found"))
    val maxSemver        = graalVersions.keys.map(version => new Semver(version)).max
    s"${graalvmKey}:${maxSemver.toString}"
  }

  private def checkJvmForIsGraalVmOrInstall(javaHome: String): String = {
    val runCommand = s"${javaHome}/bin/java --version"
    val jvmVersionInfo = ProcessExecutorService.executeToString(runCommand)
    if (jvmVersionInfo.toLowerCase.contains("graalvm")) {
      javaHome
    } else {
      installJvm().toString
    }
  }
}
