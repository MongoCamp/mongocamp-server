package dev.mongocamp.server.service

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.model.MongoCampConfiguration
import dev.mongocamp.server.model.MongoCampConfigurationExtensions._
import dev.mongocamp.server.service.ConfigurationService.{getConfig, registerNonPersistentConfig}

import scala.concurrent.duration.Duration

class ConfigSuite extends munit.FunSuite with LazyLogging {

  test("convert config with strings to database config") {
    val dbSimpleStringConfig = ConfigurationService.convertToDbConfiguration("simple.string", "String", Some("super"))
    assertEquals(dbSimpleStringConfig.configType, "String")
    assertEquals(dbSimpleStringConfig.value, "super")
    assertEquals(dbSimpleStringConfig.typedValue[String](), "super")
    assertEquals(dbSimpleStringConfig.validate, true)

    val dbSimpleStringConfig1 = ConfigurationService.convertToDbConfiguration("simple.string", "String")
    assertEquals(dbSimpleStringConfig1.configType, "String")
    assertEquals(dbSimpleStringConfig1.value, None)
    assertEquals(dbSimpleStringConfig1.validate, true)

    val dbListStringConfig = ConfigurationService.convertToDbConfiguration("list.string", "List[String]", Some(List("List", "super")))
    assertEquals(dbListStringConfig.configType, "List[String]")
    assertEquals(dbListStringConfig.validate, true)
    assertEquals(dbListStringConfig.value, List("List", "super"))
    assertEquals(dbListStringConfig.typedValue[List[String]](), List("List", "super"))

    val dbListStringConfig2 = ConfigurationService.convertToDbConfiguration("list.string", "List[String]")
    assertEquals(dbListStringConfig2.configType, "List[String]")
    assertEquals(dbListStringConfig2.validate, true)
    assertEquals(dbListStringConfig2.value, None)
  }

  test("convert config with boolean to database config") {
    val dbSimpleBooleanConfig = ConfigurationService.convertToDbConfiguration("simple.boolean", "Boolean", Some(true))
    assertEquals(dbSimpleBooleanConfig.configType, "Boolean")
    assertEquals(dbSimpleBooleanConfig.value, true)
    assertEquals(dbSimpleBooleanConfig.typedValue[Boolean](), true)
    assertEquals(dbSimpleBooleanConfig.validate, true)

    val dbSimpleBooleanConfig1 = ConfigurationService.convertToDbConfiguration("simple.boolean", "Boolean")
    assertEquals(dbSimpleBooleanConfig1.configType, "Boolean")
    assertEquals(dbSimpleBooleanConfig1.value, None)
    assertEquals(dbSimpleBooleanConfig1.validate, true)

    val dbListBooleanConfig = ConfigurationService.convertToDbConfiguration("list.boolean", "List[Boolean]", Some(List(true, false)))
    assertEquals(dbListBooleanConfig.configType, "List[Boolean]")
    assertEquals(dbListBooleanConfig.validate, true)
    assertEquals(dbListBooleanConfig.value, List(true, false))
    assertEquals(dbListBooleanConfig.typedValue[List[Boolean]](), List(true, false))

    val dbListBooleanConfig2 = ConfigurationService.convertToDbConfiguration("list.boolean", "List[Boolean]")
    assertEquals(dbListBooleanConfig2.configType, "List[Boolean]")
    assertEquals(dbListBooleanConfig2.validate, true)
    assertEquals(dbListBooleanConfig2.value, None)
  }

  test("convert config with double to database config") {
    val dbSimpleDoubleConfig = ConfigurationService.convertToDbConfiguration("simple.double", "Double", Some(1.32))
    assertEquals(dbSimpleDoubleConfig.configType, "Double")
    assertEquals(dbSimpleDoubleConfig.value, 1.32)
    assertEquals(dbSimpleDoubleConfig.typedValue[Double](), 1.32)
    assertEquals(dbSimpleDoubleConfig.validate, true)

    val dbSimpleDoubleConfig1 = ConfigurationService.convertToDbConfiguration("simple.double", "Double")
    assertEquals(dbSimpleDoubleConfig1.configType, "Double")
    assertEquals(dbSimpleDoubleConfig1.value, None)
    assertEquals(dbSimpleDoubleConfig1.validate, true)

    val dbListDoubleConfig = ConfigurationService.convertToDbConfiguration("list.double", "List[Double]", Some(List(1.0, 3.0)))
    assertEquals(dbListDoubleConfig.configType, "List[Double]")
    assertEquals(dbListDoubleConfig.validate, true)
    assertEquals(dbListDoubleConfig.value, List(1.0, 3.0))
    assertEquals(dbListDoubleConfig.typedValue[List[Double]](), List(1.0, 3.0))

    val dbListDoubleConfig2 = ConfigurationService.convertToDbConfiguration("list.double", "List[Double]")
    assertEquals(dbListDoubleConfig2.configType, "List[Double]")
    assertEquals(dbListDoubleConfig2.validate, true)
    assertEquals(dbListDoubleConfig2.value, None)
  }

  test("convert config with long to database config") {
    val dbSimpleLongConfig = ConfigurationService.convertToDbConfiguration("simple.long", "Long", Some(21L))
    assertEquals(dbSimpleLongConfig.configType, "Long")
    assertEquals(dbSimpleLongConfig.value, 21L)
    assertEquals(dbSimpleLongConfig.typedValue[Long](), 21L)
    assertEquals(dbSimpleLongConfig.validate, true)

    val dbSimpleLongConfig1 = ConfigurationService.convertToDbConfiguration("simple.long", "Long")
    assertEquals(dbSimpleLongConfig1.configType, "Long")
    assertEquals(dbSimpleLongConfig1.value, None)
    assertEquals(dbSimpleLongConfig1.validate, true)

    val dbListLongConfig = ConfigurationService.convertToDbConfiguration("list.long", "List[Long]", Some(List(4711L, 4712L)))
    assertEquals(dbListLongConfig.configType, "List[Long]")
    assertEquals(dbListLongConfig.validate, true)
    assertEquals(dbListLongConfig.value, List(4711L, 4712L))
    assertEquals(dbListLongConfig.typedValue[List[Long]](), List(4711L, 4712L))
    assertEquals(dbListLongConfig.typedValue[List[Int]](), List(4711, 4712))
    assertEquals(dbListLongConfig.typedValue[List[Double]](), List(4711.0, 4712.0))

    val dbListLongConfig2 = ConfigurationService.convertToDbConfiguration("list.long", "List[Long]")
    assertEquals(dbListLongConfig2.configType, "List[Long]")
    assertEquals(dbListLongConfig2.validate, true)
    assertEquals(dbListLongConfig2.value, None)
  }

  test("convert config with int to database config") {
    val dbSimpleIntConfig = ConfigurationService.convertToDbConfiguration("simple.int", "Int", Some(21))
    assertEquals(dbSimpleIntConfig.configType, "Long")
    assertEquals(dbSimpleIntConfig.value, 21)
    assertEquals(dbSimpleIntConfig.typedValue[Long](), 21L)
    assertEquals(dbSimpleIntConfig.validate, true)

    val dbSimpleIntConfig1 = ConfigurationService.convertToDbConfiguration("simple.int", "Int")
    assertEquals(dbSimpleIntConfig1.configType, "Long")
    assertEquals(dbSimpleIntConfig1.value, None)
    assertEquals(dbSimpleIntConfig1.validate, true)

    val dbListIntConfig = ConfigurationService.convertToDbConfiguration("list.int", "List[Int]", Some(List(4711, 4712)))
    assertEquals(dbListIntConfig.configType, "List[Long]")
    assertEquals(dbListIntConfig.validate, true)
    assertEquals(dbListIntConfig.value, List(4711, 4712))
    assertEquals(dbListIntConfig.typedValue[List[Long]](), List(4711L, 4712L))

    val dbListIntConfig2 = ConfigurationService.convertToDbConfiguration("list.int", "List[Int]")
    assertEquals(dbListIntConfig2.configType, "List[Long]")
    assertEquals(dbListIntConfig2.validate, true)
    assertEquals(dbListIntConfig2.value, None)
  }

  test("convert config with duration to database config") {
    val dbSimpleDurationConfig = ConfigurationService.convertToDbConfiguration("simple.duration", "Duration", Some(Duration("1s")))
    assertEquals(dbSimpleDurationConfig.configType, "Duration")
    assertEquals(dbSimpleDurationConfig.value, "1 second")
    assertEquals(dbSimpleDurationConfig.typedValue[Duration](), Duration("1s"))
    assertEquals(dbSimpleDurationConfig.validate, true)

    val dbSimpleDurationConfig1 = ConfigurationService.convertToDbConfiguration("simple.duration", "Duration")
    assertEquals(dbSimpleDurationConfig1.configType, "Duration")
    assertEquals(dbSimpleDurationConfig1.value, None)
    assertEquals(dbSimpleDurationConfig1.validate, true)

    val dbListDurationConfig = ConfigurationService.convertToDbConfiguration("list.duration", "List[Duration]", Some(List(Duration("2m"), Duration("3h"))))
    assertEquals(dbListDurationConfig.configType, "List[Duration]")
    assertEquals(dbListDurationConfig.validate, true)
    assertEquals(dbListDurationConfig.value, List("2 minutes", "3 hours"))
    assertEquals(dbListDurationConfig.typedValue[List[Duration]](), List(Duration("2m"), Duration("3h")))

    val dbListDurationConfig2 = ConfigurationService.convertToDbConfiguration("list.duration", "List[Duration]")
    assertEquals(dbListDurationConfig2.configType, "List[Duration]")
    assertEquals(dbListDurationConfig2.validate, true)
    assertEquals(dbListDurationConfig2.value, None)

  }

  test("register and test option string configuration without setting") {
    val testConfig = "test.config.for.me"
    registerNonPersistentConfig(testConfig, MongoCampConfiguration.confTypeStringOption)
    val config = getConfig(testConfig)
    assertEquals(config.get.configType, MongoCampConfiguration.confTypeStringOption)
    assertEquals(config.get.value, None)
    val delete = ConfigurationService.removeConfig(testConfig)
    assertEquals(delete, true)
  }

  test("register and test option string configuration with setting") {
    val testConfig2 = "TEST_CONFIG_FOR_YOU"
    System.setProperty(testConfig2, "hello world")
    registerNonPersistentConfig(testConfig2, MongoCampConfiguration.confTypeStringOption)
    val config2 = getConfig(testConfig2)
    assertEquals(config2.get.configType, MongoCampConfiguration.confTypeStringOption)
    assertEquals(config2.get.value, Some("hello world"))
    val delete = ConfigurationService.removeConfig(testConfig2)
    assertEquals(delete, true)
  }

  test("register and test option long configuration without setting") {
    val testConfig = "test.config.for.me.long"
    registerNonPersistentConfig(testConfig, MongoCampConfiguration.confTypeLongOption)
    val config = getConfig(testConfig)
    assertEquals(config.get.configType, MongoCampConfiguration.confTypeLongOption)
    assertEquals(config.get.value, None)
    val delete = ConfigurationService.removeConfig(testConfig)
    assertEquals(delete, true)
  }

  test("register and test option long configuration with setting") {
    val testConfig2 = "TEST_CONFIG_FOR_YOU_LONG"
    System.setProperty(testConfig2, "1")
    registerNonPersistentConfig(testConfig2, MongoCampConfiguration.confTypeLongOption)
    val config2 = getConfig(testConfig2)
    assertEquals(config2.get.configType, MongoCampConfiguration.confTypeLongOption)
    assertEquals(config2.get.value, Some(1))
    assertEquals(config2.get.typedValue[Option[Long]](), Some(1L))
    val delete = ConfigurationService.removeConfig(testConfig2)
    assertEquals(delete, true)
  }

}
