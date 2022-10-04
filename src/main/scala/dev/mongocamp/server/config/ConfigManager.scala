package dev.mongocamp.server.config

import com.github.blemale.scaffeine.Scaffeine
import com.typesafe.config
import com.typesafe.config.ConfigFactory
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.database.ConfigDao
import dev.mongocamp.server.exception.MongoCampException
import io.circe.parser.decode
import sttp.model.StatusCode

import scala.collection.mutable
import scala.concurrent.duration._

object ConfigManager {

  private lazy val conf: config.Config = ConfigFactory.load()

  private lazy val nonPersistentConfigs: mutable.Map[String, MongoCampConfiguration] = mutable.Map[String, MongoCampConfiguration]()

  private lazy val configCache = Scaffeine().recordStats().expireAfterWrite(15.minutes).build[String, MongoCampConfiguration]()

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
          val scalaConfigValue = conf.getValue(configKey)
          scalaConfigValue.unwrapped()
        }
        catch {
          case e: Exception =>
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
      true
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
            val scalaConfigValue = conf.getValue(configKey)
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
      checkAndUpdateWithEnv(configKey)
      insertResponse.wasAcknowledged()
    }
    else {
      false
    }
  }

  def getConfigValue[A <: Any](key: String): A = {
    getConfig(key).map(_.typedValue[A]()).getOrElse(throw MongoCampException(s"configuration for key $key not found", StatusCode.NotFound))
  }

  def getConfig(key: String): Option[MongoCampConfiguration] = {
    if (nonPersistentConfigs.contains(key)) {
      nonPersistentConfigs.get(key)
    }
    else {
      val cachedOption = configCache.getIfPresent(key)
      if (cachedOption.isDefined) {
        cachedOption
      } else {
        checkAndUpdateWithEnv(key)
        getConfigFromDatabase(key)
      }
    }
  }

  def getAllRegisteredConfigurations(): List[MongoCampConfiguration] = {
    ConfigDao()
      .find()
      .resultList()
      .map(d =>
        MongoCampConfiguration(
          d.getStringValue("key"),
          d.getValue("value"),
          d.getStringValue("configType"),
          d.getStringValue("comment"),
          d.getBoolean("needsRestartForActivation")
        )
      ) ++ nonPersistentConfigs.values
  }

  def updateConfig(key: String, value: Any, commentOption: Option[String] = None): Boolean = {
    if (loadEnvValue(key).isEmpty) {
      val dbOption = getConfigFromDatabase(key)
      if (dbOption.isDefined) {
        val mongoCampConfiguration = dbOption.get.copy(value = value, comment = commentOption.getOrElse(dbOption.get.comment))
        val replaceResult          = ConfigDao().replaceOne(Map("key" -> key), Converter.toDocument(mongoCampConfiguration)).result()
        configCache.invalidate(key)
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

  private[config] def checkAndUpdateWithEnv(key: String): Unit = {
    getConfigFromDatabase(key).foreach(dbConfig => {
      loadEnvValue(key)
        .map(s => convertStringToValue(s, dbConfig.configType))
        .map(envConfigValue => {
          if (!dbConfig.value.equals(envConfigValue)) {
            val mongoCampConfiguration = dbConfig.copy(value = envConfigValue, comment = "updated by env")
            configCache.invalidate(key)
            val replaceResult          = ConfigDao().replaceOne(Map("key" -> key), Converter.toDocument(mongoCampConfiguration)).result()
            replaceResult.wasAcknowledged()
          }
        })
    })
  }

  private[config] def getConfigFromDatabase(key: String): Option[MongoCampConfiguration] = {
    ConfigDao()
      .find(Map("key" -> key))
      .resultOption()
      .map(d =>
        MongoCampConfiguration(
          d.getStringValue("key"),
          d.getValue("value"),
          d.getStringValue("configType"),
          d.getStringValue("comment"),
          d.getBoolean("needsRestartForActivation")
        )
      )
  }

  private[config] def convertToDbConfiguration(
      configKey: String,
      configType: String,
      value: Option[Any] = None,
      comment: String = "",
      needsRestartForActivation: Boolean = false
  ): MongoCampConfiguration = {
    val invalidConfListInt = "List[Int]"
    val invalidConfInt     = "Int"

    var internalValueType = configType
    if (configType.equalsIgnoreCase(invalidConfInt)) {
      internalValueType = MongoCampConfiguration.confTypeLong
    }
    else {
      if (configType.equalsIgnoreCase(invalidConfListInt)) {
        internalValueType = s"List[${MongoCampConfiguration.confTypeLong}]"
      }
    }
    val isIntType = configType.equalsIgnoreCase(invalidConfInt) || configType.toLowerCase.contains(invalidConfInt.toLowerCase)
    val isLongType =
      configType.equalsIgnoreCase(MongoCampConfiguration.confTypeLong) || configType.toLowerCase.contains(MongoCampConfiguration.confTypeLong.toLowerCase)
    val isDoubleType =
      configType.equalsIgnoreCase(MongoCampConfiguration.confTypeDouble) || configType.toLowerCase.contains(MongoCampConfiguration.confTypeDouble.toLowerCase)
    var configValue = value
    if (value.isDefined) {
      value.get match {
        case l: List[_] =>
          if (l.isEmpty) {
            internalValueType = s"List[${MongoCampConfiguration.confTypeString}]"
          }
          else {
            l.head match {
              case s: String =>
                internalValueType = s"List[${MongoCampConfiguration.confTypeString}]"
                if (isIntType || isLongType) {
                  configValue = Some(l.map(_.toString.toLong))
                  internalValueType = s"List[${MongoCampConfiguration.confTypeLong}]"
                }
                if (isDoubleType) {
                  configValue = Some(l.map(_.toString.toDouble))
                  internalValueType = s"List[${MongoCampConfiguration.confTypeDouble}]"
                }
              case _: Boolean =>
                internalValueType = s"List[${MongoCampConfiguration.confTypeBoolean}]"
              case _: Double =>
                internalValueType = s"List[${MongoCampConfiguration.confTypeDouble}]"
              case _: Long =>
                internalValueType = s"List[${MongoCampConfiguration.confTypeLong}]"
              case _: Int =>
                internalValueType = s"List[${MongoCampConfiguration.confTypeLong}]"
              case _: Duration =>
                internalValueType = s"List[${MongoCampConfiguration.confTypeDuration}]"
                configValue = Some(l.map(_.asInstanceOf[Duration].toString))
            }
          }
        case o: Option[_] =>
          if (o.isEmpty) {
            internalValueType = s"Option[${MongoCampConfiguration.confTypeString}]"
          }
          else {
            o.head match {
              case s: String =>
                internalValueType = s"Option[${MongoCampConfiguration.confTypeString}]"
                if (isIntType || isLongType) {
                  configValue = Some(o.map(_.toString.toLong))
                  internalValueType = s"Option[${MongoCampConfiguration.confTypeLong}]"
                }
                if (isDoubleType) {
                  configValue = Some(o.map(_.toString.toDouble))
                  internalValueType = s"Option[${MongoCampConfiguration.confTypeDouble}]"
                }
              case _: Boolean =>
                internalValueType = s"Option[${MongoCampConfiguration.confTypeBoolean}]"
              case _: Double =>
                internalValueType = s"Option[${MongoCampConfiguration.confTypeDouble}]"
              case _: Long =>
                internalValueType = s"Option[${MongoCampConfiguration.confTypeLong}]"
              case _: Int =>
                internalValueType = s"Option[${MongoCampConfiguration.confTypeLong}]"
              case _: Duration =>
                internalValueType = s"Option[${MongoCampConfiguration.confTypeDuration}]"
                configValue = Some(o.map(_.asInstanceOf[Duration].toString))
            }
          }
        case s: String =>
          internalValueType = MongoCampConfiguration.confTypeString
          if (isIntType || isLongType) {
            configValue = s.toLongOption
            internalValueType = MongoCampConfiguration.confTypeLong
          }
          if (isDoubleType) {
            configValue = s.toDoubleOption
            internalValueType = MongoCampConfiguration.confTypeDouble
          }
        case _: Boolean =>
          internalValueType = MongoCampConfiguration.confTypeBoolean
        case _: Double =>
          internalValueType = MongoCampConfiguration.confTypeDouble
        case _: Long =>
          internalValueType = MongoCampConfiguration.confTypeLong
        case _: Int =>
          internalValueType = MongoCampConfiguration.confTypeLong
        case duration: Duration =>
          internalValueType = MongoCampConfiguration.confTypeDuration
          configValue = Some(duration.toString)
        case _ =>
      }
    }

    def generateConfigurationInternal: MongoCampConfiguration = {
      var configurationValue = configValue.getOrElse(None)
      if (configurationValue.isInstanceOf[Some[_]]) {
        configurationValue = configurationValue.asInstanceOf[Some[_]].get
      }
      val dbConfiguration = MongoCampConfiguration(
        key = configKey,
        value = configurationValue,
        configType = internalValueType,
        comment = s"$comment",
        needsRestartForActivation
      )
      if (dbConfiguration.validate) {
        dbConfiguration
      }
      else {
        throw MongoCampException(s"invalid configuration type $configType", StatusCode.PreconditionFailed)
      }
    }

    if (configType.equalsIgnoreCase(internalValueType) || isIntType) {
      generateConfigurationInternal
    }
    else {
      if (configType.toLowerCase.contains("Option".toLowerCase) || configType.toLowerCase.contains(internalValueType.toLowerCase)) {
        val configValue = generateConfigurationInternal
        if (!configValue.value.isInstanceOf[Option[_]]) {
          val myConfType = if (configValue.configType.toLowerCase.contains("Option".toLowerCase)) {
            configValue.configType
          }
          else {
            s"Option[${configValue.configType}]"
          }
          configValue.copy(value = Option(configValue.value), configType = myConfType)
        }
        else {
          configValue
        }
      }
      else {
        throw MongoCampException(s"invalid configuration value $configValue for type $configType", StatusCode.PreconditionFailed)
      }
    }
  }

  private[config] def convertStringToValue(stringValue: String, configType: String): Any = {
    configType match {
      case s"List[${MongoCampConfiguration.confTypeString}]"  => decode[List[String]](stringValue).getOrElse(List())
      case s"List[${MongoCampConfiguration.confTypeBoolean}]" => decode[List[Boolean]](stringValue).getOrElse(List())
      case s"List[${MongoCampConfiguration.confTypeDouble}]"  => decode[List[Double]](stringValue).getOrElse(List())
      case s"List[${MongoCampConfiguration.confTypeLong}]"    => decode[List[Long]](stringValue).getOrElse(List())
      case s"List[${MongoCampConfiguration.confTypeDuration}]" =>
        decode[List[String]](stringValue).getOrElse(List()).map(durationString => Duration(durationString))
      case MongoCampConfiguration.confTypeString   => stringValue
      case MongoCampConfiguration.confTypeBoolean  => stringValue.toBooleanOption.getOrElse(false)
      case MongoCampConfiguration.confTypeDouble   => stringValue.toDoubleOption.getOrElse(false)
      case MongoCampConfiguration.confTypeLong     => stringValue.toLongOption.getOrElse(false)
      case MongoCampConfiguration.confTypeDuration => Duration(stringValue)
    }

  }

  private def loadEnvValue(key: String): Option[String] = {
    val systemSettingKey = key.toUpperCase().replace(".", "_")
    val envSetting       = System.getenv(systemSettingKey)
    if (envSetting != null && !"".equalsIgnoreCase(envSetting.trim)) {
      Some(envSetting)
    }
    else {
      val propertySetting = System.getProperty(systemSettingKey)
      if (propertySetting != null && !"".equalsIgnoreCase(propertySetting.trim)) {
        Some(propertySetting)
      }
      else {
        None
      }
    }
  }

}
