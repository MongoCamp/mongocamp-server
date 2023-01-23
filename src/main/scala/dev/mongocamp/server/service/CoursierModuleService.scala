package dev.mongocamp.server.service

import better.files.File
import com.typesafe.scalalogging.LazyLogging
import coursier.core.Authentication
import coursier.ivy.IvyRepository
import coursier.params.ResolutionParams
import coursier.{Dependency, _}
import dev.mongocamp.server.BuildInfo
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.service.ReflectionService.registerClassLoaders
import io.circe.generic.auto._
import io.circe.parser._
import org.reflections.vfs.Vfs

import scala.collection.mutable.ArrayBuffer
import scala.reflect.internal.util.ScalaClassLoader.URLClassLoader

object CoursierModuleService extends LazyLogging {

  // todo handling when loading of single plugin failed

  def resolvePlugins(): List[File] = {
    try {
      val baseScala = {
        val list = BuildInfo.scalaVersion.split('.')
        s"${list(0)}.${list(1)}"
      }
      val params = ResolutionParams()
        .withScalaVersion(BuildInfo.scalaVersion)
        .addExclusions((org"dev.mongocamp", ModuleName(s"mongocamp-server_$baseScala")))
        .addExclusions((org"org.scala-lang", ModuleName("scala-library")))

      val dependencies: List[Dependency] = ConfigurationService.getConfigValue[List[String]](DefaultConfigurations.ConfigKeyPluginsModules).map(s => dep"$s")
      val mvnRepository: List[MavenRepository] = ConfigurationService.getConfigValue[List[String]](DefaultConfigurations.ConfigKeyPluginsMavenRepositories).map(string => dev.mongocamp.server.plugin.coursier.Repository.mvn(string))

      val resolution = Fetch()
        .withDependencies(dependencies)
        .withRepositories(mvnRepository)
        .addRepositories(
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
        .withResolutionParams(params)
        .run()
      resolution.toList.map(jFile => File(jFile.toURI))
    }
    catch {
      case e: Exception =>
        logger.error(e.getMessage, e)
        List()

    }
  }
}
