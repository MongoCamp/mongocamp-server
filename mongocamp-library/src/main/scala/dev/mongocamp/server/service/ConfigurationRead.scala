package dev.mongocamp.server.service

import com.github.blemale.scaffeine.Scaffeine
import com.typesafe.config
import com.typesafe.config.ConfigFactory
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.database.ConfigDao
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.model.MongoCampConfiguration
import dev.mongocamp.server.model.MongoCampConfigurationExtensions._
import dev.mongocamp.server.service.ConfigurationRead.{configCache, nonPersistentConfigs}
import io.circe.parser.decode
import org.mongodb.scala.bson.Document
import sttp.model.StatusCode

import scala.collection.mutable
import scala.concurrent.duration._
trait ConfigurationRead {

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
      }
      else {
        checkAndUpdateWithEnv(key)
        getConfigFromDatabase(key)
      }
    }
  }

  def getAllRegisteredConfigurations(): List[MongoCampConfiguration] = {
    val databaseConfigs = ConfigDao()
      .find(Map(), Map("key" -> 1))
      .resultList()
      .map(convertDocumentToConfiguration)
    val response = databaseConfigs ++ nonPersistentConfigs.values
    response.sortBy(_.key)
  }

  private[service] def checkAndUpdateWithEnv(key: String): Unit = {
    getConfigFromDatabase(key).foreach(dbConfig => {
      loadEnvValue(key)
        .map(s => convertStringToValue(s, dbConfig.configType))
        .map(envConfigValue => {
          if (dbConfig.value == null || !dbConfig.value.equals(envConfigValue)) {
            val mongoCampConfiguration = dbConfig.copy(value = envConfigValue, comment = "updated by env")
            val replaceResult          = ConfigDao().replaceOne(Map("key" -> key), Converter.toDocument(mongoCampConfiguration)).result()
            configCache.invalidate(key)
            publishConfigUpdateEvent(key, envConfigValue, dbConfig.value, "checkAndUpdateWithEnv")
            replaceResult.wasAcknowledged()
          }
        })
    })
  }

  protected def publishConfigUpdateEvent(key: String, newValue: Any, oldValue: Any, callingMethod: String): Unit

  private[service] def getConfigFromDatabase(key: String): Option[MongoCampConfiguration] = {
    ConfigDao()
      .find(Map("key" -> key))
      .resultOption()
      .map(convertDocumentToConfiguration)
  }

  private def convertDocumentToConfiguration(d: Document): MongoCampConfiguration = {
    MongoCampConfiguration(
      d.getStringValue("key"),
      d.getValue("value"),
      d.getStringValue("configType"),
      d.getStringValue("comment"),
      d.getBoolean("needsRestartForActivation")
    )
  }

  private[service] def convertToDbConfiguration(
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
        internalValueType = MongoCampConfiguration.confTypeLongList
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
            internalValueType = MongoCampConfiguration.confTypeStringList
          }
          else {
            l.head match {
              case s: String =>
                internalValueType = MongoCampConfiguration.confTypeStringList
                if (isIntType || isLongType) {
                  configValue = Some(l.map(_.toString.toLong))
                  internalValueType = MongoCampConfiguration.confTypeLongList
                }
                if (isDoubleType) {
                  configValue = Some(l.map(_.toString.toDouble))
                  internalValueType = MongoCampConfiguration.confTypeDoubleList
                }
              case _: Boolean =>
                internalValueType = MongoCampConfiguration.confTypeBooleanList
              case _: Double =>
                internalValueType = MongoCampConfiguration.confTypeDoubleList
              case _: Long =>
                internalValueType = MongoCampConfiguration.confTypeLongList
              case _: Int =>
                internalValueType = MongoCampConfiguration.confTypeLongList
              case _: Duration =>
                internalValueType = MongoCampConfiguration.confTypeDurationList
                configValue = Some(l.map(_.asInstanceOf[Duration].toString))
            }
          }
        case o: Option[_] =>
          if (o.isEmpty) {
            internalValueType = MongoCampConfiguration.confTypeStringOption
          }
          else {
            o.head match {
              case s: String =>
                internalValueType = MongoCampConfiguration.confTypeStringOption
                if (isIntType || isLongType) {
                  configValue = Some(o.map(_.toString.toLong))
                  internalValueType = MongoCampConfiguration.confTypeLongOption
                }
                if (isDoubleType) {
                  configValue = Some(o.map(_.toString.toDouble))
                  internalValueType = MongoCampConfiguration.confTypeDoubleOption
                }
              case _: Boolean =>
                internalValueType = MongoCampConfiguration.confTypeBooleanOption
              case _: Double =>
                internalValueType = MongoCampConfiguration.confTypeDoubleOption
              case _: Long =>
                internalValueType = MongoCampConfiguration.confTypeLongOption
              case _: Int =>
                internalValueType = MongoCampConfiguration.confTypeLongOption
              case _: Duration =>
                internalValueType = MongoCampConfiguration.confTypeDurationOption
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

  private[service] def convertStringToValue(stringValue: String, configType: String): Any = {

    configType match {
      case MongoCampConfiguration.confTypeStringList  => decode[List[String]](stringValue).getOrElse(List())
      case MongoCampConfiguration.confTypeBooleanList => decode[List[Boolean]](stringValue).getOrElse(List())
      case MongoCampConfiguration.confTypeDoubleList  => decode[List[Double]](stringValue).getOrElse(List())
      case MongoCampConfiguration.confTypeLongList    => decode[List[Long]](stringValue).getOrElse(List())
      case MongoCampConfiguration.confTypeDurationList =>
        decode[List[String]](stringValue).getOrElse(List()).map(durationString => Duration(durationString))
      case MongoCampConfiguration.confTypeString   => stringValue
      case MongoCampConfiguration.confTypeBoolean  => stringValue.toBooleanOption.getOrElse(false)
      case MongoCampConfiguration.confTypeDouble   => stringValue.toDoubleOption.getOrElse(false)
      case MongoCampConfiguration.confTypeLong     => stringValue.toLongOption.getOrElse(false)
      case MongoCampConfiguration.confTypeDuration => Duration(stringValue)
    }

  }

  protected def loadEnvValue(key: String): Option[String] = {
    val envSetting = System.getenv(key)
    if (envSetting != null && !"".equalsIgnoreCase(envSetting.trim)) {
      Some(envSetting)
    }
    else {
      val propertySetting = System.getProperty(key)
      if (propertySetting != null && !"".equalsIgnoreCase(propertySetting.trim)) {
        Some(propertySetting)
      }
      else {
        None
      }
    }
  }

}

object ConfigurationRead {

  private[service] lazy val conf: config.Config = ConfigFactory.load()

  private[service] lazy val nonPersistentConfigs: mutable.Map[String, MongoCampConfiguration] = mutable.Map[String, MongoCampConfiguration]()

  private[service] lazy val configCache = Scaffeine().recordStats().expireAfterWrite(15.minutes).build[String, MongoCampConfiguration]()

}