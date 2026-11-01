package dev.mongocamp.server.service

import better.files.File
import com.typesafe.scalalogging.LazyLogging
import com.vdurmont.semver4j.Semver
import coursier._
import coursier.core.Configuration
import coursier.params.ResolutionParams
import coursier.parse.DependencyParser
import coursier.util.StringInterpolators.*
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.library.BuildInfo

object CoursierModuleService extends LazyLogging {

  // Scala 3 artifacts end with _3, Scala 2 artifacts with the minor version (_2.13, _2.12)
  private[service] def scalaBinaryVersion(scalaVersion: String): String = {
    val semVersion = new Semver(scalaVersion)
    if (semVersion.getMajor >= 3) semVersion.getMajor.toString else s"${semVersion.getMajor}.${semVersion.getMinor}"
  }

  private[service] def serverModuleName: ModuleName = ModuleName(s"mongocamp-server_${scalaBinaryVersion(BuildInfo.scalaVersion)}")

  // the server and the scala library are already part of the running server, plugins must not bring their own copy
  private[service] lazy val resolutionExclusions: Set[(Organization, ModuleName)] = {
    val serverExclusion = Set("2.12", "2.13", "3").map(
      baseScala => (org"dev.mongocamp", ModuleName(s"mongocamp-server_$baseScala"))
    )
    serverExclusion ++ Set((org"org.scala-lang", ModuleName("scala-library")), (org"org.scala-lang", ModuleName("scala3-library_3")))
  }

  private lazy val resolutionParams: ResolutionParams = {
    ResolutionParams()
      .withScalaVersion(BuildInfo.scalaVersion)
      .addExclusions(resolutionExclusions.toSeq*)
  }

  private lazy val defaultRepositories: List[Repository] = {
    List(
      Repositories.central,
      Repositories.jcenter,
      Repositories.jitpack,
      Repositories.google,
      Repositories.sonatype("releases"),
      Repositories.sonatype("snapshots"),
      Repositories.centralGcs,
      Repositories.centralGcsAsia,
      Repositories.centralGcsEu,
      Repositories.clojars
    )
  }

  def loadMavenConfiguredDependencies(): List[File] = {
    val modules = ConfigurationRead.noPublishReader.getConfigValue[List[String]](DefaultConfigurations.ConfigKeyPluginsModules)
    loadMavenConfiguredDependencies(
      modules.map(
        mName => mName.replace("$$MC_VERSION$$", BuildInfo.version)
      )
    )
  }

  def loadMavenConfiguredDependencies(dependencyStrings: List[String]): List[File] = {
    val dependencies: List[Dependency] = dependencyStrings.map(
      s =>
        DependencyParser
          .dependency(s, scala.util.Properties.versionNumberString, Configuration.empty)
          .getOrElse(throw new Exception(s"$s is not a right configured maven dependency"))
    )
    fetchMavenDependencies(dependencies, Some(resolutionParams), true)
  }

  def loadServerWithAllDependencies(): List[File] = {
    val dependency = Dependency(Module(Organization(BuildInfo.organization), serverModuleName), BuildInfo.version)
    CoursierModuleService.fetchMavenDependencies(List(dependency), None, useCustomMavenRepos = false)
  }

  private def fetchMavenDependencies(dependencies: List[Dependency], resolutionParams: Option[ResolutionParams], useCustomMavenRepos: Boolean): List[File] = {
    try {
      var fetchCommand = Fetch().withDependencies(dependencies).addRepositories(defaultRepositories: _*)

      resolutionParams.foreach(
        params => fetchCommand = fetchCommand.withResolutionParams(params)
      )

      fetchCommand = fetchCommand.addRepositories(getConfiguredMavenRepositories(useCustomMavenRepos): _*)

      val resolution = fetchCommand.run()
      resolution.toList.map(
        jFile => File(jFile.toURI)
      )
    }
    catch {
      case _: Exception =>
        fetchMavenDependencies(checkMavenDependencies(dependencies, useCustomMavenRepos), resolutionParams, useCustomMavenRepos)
    }
  }

  private def getConfiguredMavenRepositories(useCustomMavenRepos: Boolean): List[MavenRepository] = {
    if (useCustomMavenRepos) {
      val configRead = ConfigurationRead.noPublishReader
      val mvnRepository: List[MavenRepository] = configRead
        .getConfigValue[List[String]](DefaultConfigurations.ConfigKeyPluginsMavenRepositories)
        .map(
          string => dev.mongocamp.server.plugin.coursier.Repository.mvn(string)
        )
      mvnRepository
    }
    else {
      List.empty
    }
  }

  private def checkMavenDependencies(dependencies: List[Dependency], useCustomMavenRepos: Boolean): List[Dependency] = {
    val mvnRepository: List[MavenRepository] = getConfiguredMavenRepositories(useCustomMavenRepos)
    dependencies.filter(
      dependency => {
        try {
          val resolution = Fetch()
            .addDependencies(dependency)
            .addRepositories(mvnRepository: _*)
            .addRepositories(defaultRepositories: _*)
            .withResolutionParams(resolutionParams)
            .run()
          resolution.nonEmpty
        }
        catch {
          case e: Exception =>
            logger.error(e.getMessage, e)
            false
        }
      }
    )

  }
}
