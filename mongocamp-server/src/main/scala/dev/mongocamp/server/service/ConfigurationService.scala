package dev.mongocamp.server.service

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.database.ConfigDao
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.event.config.{ ConfigRegisterEvent, ConfigUpdateEvent }
import dev.mongocamp.server.service.ConfigurationRead.{ configCache, nonPersistentConfigs }

object ConfigurationService extends ConfigurationRead {

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

  def updateConfig(key: String, value: Any, commentOption: Option[String] = None): Boolean = {
    if (loadEnvValue(key).isEmpty) {
      val dbOption = getConfigFromDatabase(key)
      if (dbOption.isDefined) {
        val mongoCampConfiguration = dbOption.get.copy(value = value, comment = commentOption.getOrElse(dbOption.get.comment))
        val replaceResult          = ConfigDao().replaceOne(Map("key" -> key), Converter.toDocument(mongoCampConfiguration)).result()
        configCache.invalidate(key)
        EventSystem.publish(ConfigUpdateEvent(key, value, dbOption.get.value, "updateConfig"))
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
    EventSystem.publish(ConfigUpdateEvent(key, newValue, oldValue, "checkAndUpdateWithEnv"))
  }

  override protected def publishConfigRegisterEvent(
      persistent: Boolean,
      configKey: String,
      configType: String,
      value: Option[Any],
      comment: String,
      needsRestartForActivation: Boolean
  ): Unit = {
    EventSystem.publish(ConfigRegisterEvent(persistent, configKey, configType, value, comment, needsRestartForActivation))
  }
}
