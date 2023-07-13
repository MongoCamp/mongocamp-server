package dev.mongocamp.server.service

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.config.DefaultConfigurations._
import dev.mongocamp.server.database.ConfigDao
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.event.config.{ConfigRegisterEvent, ConfigUpdateEvent}
import dev.mongocamp.server.model.MongoCampConfiguration
import dev.mongocamp.server.service.ConfigurationRead.{conf, configCache, nonPersistentConfigs}

import scala.util.Random

object ConfigurationService extends ConfigurationRead {

  def registerNonPersistentConfig(configKey: String, configType: String, value: Option[Any] = None, comment: String = ""): Boolean = {
    val internalValue = if (value.isDefined) {
      value.get
    }
    else {
      val envValue = loadEnvValue(configKey)
      if (envValue.isDefined) {
        envValue.get
      }
      else {
        try {
          val key              = configKey.toLowerCase().replace("_", ".")
          val scalaConfigValue = conf.getValue(key)
          scalaConfigValue.unwrapped()
        }
        catch {
          case _: Exception =>
            null
        }
      }
    }
    val config = convertToDbConfiguration(configKey, configType, Option(internalValue).filterNot(_.toString.isEmpty), comment, needsRestartForActivation = true)
    if (nonPersistentConfigs.keys.toList.contains(configKey)) {
      false
    }
    else {
      nonPersistentConfigs.put(configKey, config)
      EventSystem.eventStream.publish(ConfigRegisterEvent(persistent = false, configKey, configType, value, comment, config.needsRestartForActivation))
      true
    }
  }

  def removeConfig(configKey: String): Boolean = {
    val nonPersistentRemove = nonPersistentConfigs.remove(configKey)
    if (nonPersistentRemove.isEmpty) {
      configCache.invalidate(configKey)
      val deleteResult = ConfigDao().deleteOne(Map("key" -> configKey)).result()
      deleteResult.wasAcknowledged()
    }
    else {
      nonPersistentRemove.nonEmpty
    }
  }

  def registerConfig(
      configKey: String,
      configType: String,
      value: Option[Any] = None,
      comment: String = "",
      needsRestartForActivation: Boolean = false
  ): Boolean = {
    if (getConfigFromDatabase(configKey).isEmpty) {
      val dbConfiguration: MongoCampConfiguration = convertToDbConfiguration(configKey, configType, value, comment, needsRestartForActivation)
      val configToInsert: MongoCampConfiguration = {
        try {
          if (value.isEmpty) {
            val key              = configKey.toLowerCase().replace("_", ".")
            val scalaConfigValue = conf.getValue(key)
            dbConfiguration.copy(value = scalaConfigValue.unwrapped())
          }
          else {
            dbConfiguration
          }
        }
        catch {
          case _: Exception =>
            dbConfiguration
        }
      }
      val insertResponse = ConfigDao().insertOne(Converter.toDocument(configToInsert)).result()
      EventSystem.eventStream.publish(
        ConfigRegisterEvent(persistent = true, configKey, configType, value, comment, needsRestartForActivation = needsRestartForActivation)
      )
      checkAndUpdateWithEnv(configKey)
      insertResponse.wasAcknowledged()
    }
    else {
      false
    }
  }

  def updateConfig(key: String, value: Any, commentOption: Option[String] = None): Boolean = {
    if (loadEnvValue(key).isEmpty) {
      val dbOption = getConfigFromDatabase(key)
      if (dbOption.isDefined) {
        val mongoCampConfiguration = dbOption.get.copy(value = value, comment = commentOption.getOrElse(dbOption.get.comment))
        val replaceResult          = ConfigDao().replaceOne(Map("key" -> key), Converter.toDocument(mongoCampConfiguration)).result()
        configCache.invalidate(key)
        EventSystem.eventStream.publish(ConfigUpdateEvent(key, value, dbOption.get.value, "updateConfig"))
        replaceResult.wasAcknowledged()
      }
      else {
        false
      }
    }
    else {
      false
    }
  }

  override protected def publishConfigUpdateEvent(key: String, newValue: Any, oldValue: Any, callingMethod: String): Unit = {
    EventSystem.eventStream.publish(ConfigUpdateEvent(key, newValue, oldValue, "checkAndUpdateWithEnv"))
  }

  def registerMongoCampServerDefaultConfigs(): Unit = {
    registerNonPersistentConfig(ConfigKeyConnectionHost, MongoCampConfiguration.confTypeString)
    registerNonPersistentConfig(ConfigKeyConnectionPort, MongoCampConfiguration.confTypeLong)
    registerNonPersistentConfig(ConfigKeyConnectionDatabase, MongoCampConfiguration.confTypeString)
    registerNonPersistentConfig(ConfigKeyConnectionUsername, MongoCampConfiguration.confTypeStringOption)
    registerNonPersistentConfig(ConfigKeyConnectionPassword, MongoCampConfiguration.confTypeStringOption)
    registerNonPersistentConfig(ConfigKeyConnectionAuthDb, MongoCampConfiguration.confTypeString)
    registerNonPersistentConfig(ConfigKeyAuthPrefix, MongoCampConfiguration.confTypeString)
    registerNonPersistentConfig(ConfigKeyAuthUsers, MongoCampConfiguration.confTypeStringList)
    registerNonPersistentConfig(ConfigKeyAuthRoles, MongoCampConfiguration.confTypeStringList)

    ConfigDao().createUniqueIndexForField("key").result()

    registerConfig(ConfigKeyServerInterface, MongoCampConfiguration.confTypeString, needsRestartForActivation = true)
    registerConfig(ConfigKeyServerPort, MongoCampConfiguration.confTypeLong, needsRestartForActivation = true)

    registerConfig(ConfigKeyPluginsIgnored, MongoCampConfiguration.confTypeStringList, needsRestartForActivation = true)
    registerConfig(ConfigKeyPluginsUrls, MongoCampConfiguration.confTypeStringList, needsRestartForActivation = true)
    registerConfig(ConfigKeyPluginsDirectory, MongoCampConfiguration.confTypeString, needsRestartForActivation = true)
    registerConfig(ConfigKeyPluginsModules, MongoCampConfiguration.confTypeStringList, needsRestartForActivation = true)
    registerConfig(ConfigKeyPluginsMavenRepositories, MongoCampConfiguration.confTypeStringList, needsRestartForActivation = true)

    registerConfig(ConfigKeyHttpClientHeaders, MongoCampConfiguration.confTypeString)

    registerConfig(ConfigKeyAuthHandler, MongoCampConfiguration.confTypeString, needsRestartForActivation = true)
    registerConfig(ConfigKeyAuthSecret, MongoCampConfiguration.confTypeString, Some(Random.alphanumeric.take(32).mkString))
    registerConfig(ConfigKeyAuthApiKeyLength, MongoCampConfiguration.confTypeLong)
    registerConfig(ConfigKeyAuthCacheDb, MongoCampConfiguration.confTypeBoolean)
    registerConfig(ConfigKeyAuthExpiringDuration, MongoCampConfiguration.confTypeDuration)

    registerConfig(ConfigKeyAuthBearer, MongoCampConfiguration.confTypeBoolean, needsRestartForActivation = true)
    registerConfig(ConfigKeyAuthBasic, MongoCampConfiguration.confTypeBoolean, needsRestartForActivation = true)
    registerConfig(ConfigKeyAuthToken, MongoCampConfiguration.confTypeBoolean, needsRestartForActivation = true)

    registerConfig(ConfigKeyFileHandler, MongoCampConfiguration.confTypeString, needsRestartForActivation = true)
    registerConfig(ConfigKeyFileCache, MongoCampConfiguration.confTypeString)

    registerConfig(ConfigKeyCorsHeadersAllowed, MongoCampConfiguration.confTypeStringList)
    registerConfig(ConfigKeyCorsHeadersExposed, MongoCampConfiguration.confTypeStringList)
    registerConfig(ConfigKeyCorsOriginsAllowed, MongoCampConfiguration.confTypeStringList)

    registerConfig(ConfigKeyDocsSwagger, MongoCampConfiguration.confTypeBoolean, needsRestartForActivation = true)
    registerConfig(ConfigKeyOpenApi, MongoCampConfiguration.confTypeBoolean, needsRestartForActivation = true)
  }

}
