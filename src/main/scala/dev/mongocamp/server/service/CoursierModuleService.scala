package dev.mongocamp.server.service

import better.files.File
import com.typesafe.scalalogging.LazyLogging
import coursier.params.ResolutionParams
import coursier.{ Dependency, _ }
import dev.mongocamp.server.BuildInfo
import dev.mongocamp.server.config.DefaultConfigurations

object CoursierModuleService extends LazyLogging {

  private lazy val resolutionParams: ResolutionParams = {
    val serverExclusion = Set("2.12", "2.13").map(baseScala => (org"dev.mongocamp", ModuleName(s"mongocamp-server_$baseScala")))
    ResolutionParams()
      .withScalaVersion(BuildInfo.scalaVersion)
      .withExclusions(serverExclusion)
      .addExclusions((org"org.scala-lang", ModuleName("scala-library")))
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
    val dependencies: List[Dependency] = ConfigurationService.getConfigValue[List[String]](DefaultConfigurations.ConfigKeyPluginsModules).map(s => dep"$s")
    fetchMavenDependencies(dependencies)
  }

  private def fetchMavenDependencies(dependencies: List[Dependency]): List[File] = {
    try {
      val mvnRepository: List[coursier.MavenRepository] = getConfiguredMavenRepositories
      val resolution = Fetch()
        .withDependencies(dependencies)
        .withRepositories(mvnRepository)
        .withRepositories(defaultRepositories)
        .withResolutionParams(resolutionParams)
        .run()
      resolution.toList.map(jFile => File(jFile.toURI))
    }
    catch {
      case _: Exception =>
        fetchMavenDependencies(checkMavenDependencies(dependencies))
    }
  }

  private def getConfiguredMavenRepositories: List[MavenRepository] = {
    val mvnRepository: List[MavenRepository] = ConfigurationService
      .getConfigValue[List[String]](DefaultConfigurations.ConfigKeyPluginsMavenRepositories)
      .map(string => dev.mongocamp.server.plugin.coursier.Repository.mvn(string))
    mvnRepository
  }

  private def checkMavenDependencies(dependencies: List[Dependency]): List[Dependency] = {
    val mvnRepository: List[MavenRepository] = getConfiguredMavenRepositories
    dependencies.filter(dependency => {
      try {
        val resolution = Fetch()
          .addDependencies(dependency)
          .withRepositories(mvnRepository)
          .withRepositories(defaultRepositories)
          .withResolutionParams(resolutionParams)
          .run()
        resolution.nonEmpty
      }
      catch {
        case e: Exception =>
          logger.error(e.getMessage, e)
          false
      }
    })

  }
}
