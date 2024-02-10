package dev.mongocamp.server.service

import com.typesafe.scalalogging.LazyLogging

class CoursierModuleSuite extends munit.FunSuite with LazyLogging {

  test("fetch dependencies for mongocamp driver") {
    val mavenFiles = CoursierModuleService.loadMavenConfiguredDependencies(List("dev.mongocamp:mongodb-driver_2.13:2.5.3"))
    assertEquals(mavenFiles.size, 15)
  }

  test("fetch dependencies for sample plugin with own repository") {
    System.setProperty("PLUGINS_MAVEN_REPOSITORIES", "[]")
    val samplePlugin = "org.springframework:spring-core:6.0.0-M1"
    val mavenFilesWithoutRegistry = CoursierModuleService.loadMavenConfiguredDependencies(List(samplePlugin))
    assertEquals(mavenFilesWithoutRegistry.size, 0)
    System.setProperty("PLUGINS_MAVEN_REPOSITORIES", "[\"https://repo.spring.io/milestone\"]")
    val mavenFiles = CoursierModuleService.loadMavenConfiguredDependencies(List(samplePlugin))
    assertEquals(mavenFiles.size, 2)
  }

}
