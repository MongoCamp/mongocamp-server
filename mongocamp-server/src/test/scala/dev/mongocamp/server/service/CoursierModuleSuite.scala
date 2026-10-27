package dev.mongocamp.server.service

import com.typesafe.scalalogging.LazyLogging

class CoursierModuleSuite extends munit.FunSuite with LazyLogging {

  test("scala binary version of the artifact suffix") {
    assertEquals(CoursierModuleService.scalaBinaryVersion("3.9.0"), "3")
    assertEquals(CoursierModuleService.scalaBinaryVersion("3.3.9"), "3")
    assertEquals(CoursierModuleService.scalaBinaryVersion("2.13.18"), "2.13")
    assertEquals(CoursierModuleService.scalaBinaryVersion("2.12.21"), "2.12")
  }

  test("server module name matches the artifact published by this build") {
    // publishLocal publishes mongocamp-server_<binary version of the build>, loadServerWithAllDependencies has to ask for exactly that
    assertEquals(CoursierModuleService.serverModuleName.value, "mongocamp-server_3")
  }

  test("plugin resolution excludes the server and the scala libraries for every scala version") {
    val excluded = CoursierModuleService.resolutionExclusions.map(e => s"${e._1.value}:${e._2.value}")
    List("mongocamp-server_2.12", "mongocamp-server_2.13", "mongocamp-server_3").foreach(
      name => assert(excluded.contains(s"dev.mongocamp:$name"), s"$name is not excluded")
    )
    assert(excluded.contains("org.scala-lang:scala-library"))
    assert(excluded.contains("org.scala-lang:scala3-library_3"))
  }

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
