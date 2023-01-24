package dev.mongocamp.server.service

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.model.MongoCampConfiguration
import dev.mongocamp.server.model.MongoCampConfigurationExtensions._
import dev.mongocamp.server.service.ConfigurationService.{getConfig, registerNonPersistentConfig}
import dev.mongocamp.server.tests.BaseSuite

import scala.concurrent.duration.Duration

class CoursierModuleSuite extends BaseSuite {

  test("fetch dependencies for mongocamp driver") {
    val mavenFiles = CoursierModuleService.loadMavenConfiguredDependencies(List("dev.mongocamp:mongodb-driver_2.13:2.5.3"))
    assertEquals(mavenFiles.size, 15)
  }

  test("fetch dependencies for sample plugin with own repository") {
    val samplePlugin = "dev.mongocamp:mongocamp-sample-plugin_2.13:0.6.0"
    val mavenFilesWithoutRegistry = CoursierModuleService.loadMavenConfiguredDependencies(List(samplePlugin))
    assertEquals(mavenFilesWithoutRegistry.size, 0)
    System.setProperty("PLUGINS_MAVEN_REPOSITORIES", "[\"https://maven.pkg.github.com/mongocamp/mongocamp-sample-plugin/\"]")
    val mavenFiles = CoursierModuleService.loadMavenConfiguredDependencies(List(samplePlugin))
    assertEquals(mavenFiles.size, 1)
  }


}
