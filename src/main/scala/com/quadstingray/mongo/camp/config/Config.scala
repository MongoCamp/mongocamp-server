package com.quadstingray.mongo.camp.config

import com.typesafe.config
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import io.circe.parser._

import java.time.Duration
import scala.concurrent.duration.{ Duration => ScalaDuration }
import scala.jdk.CollectionConverters._

trait Config extends LazyLogging {

  private lazy val conf: config.Config = ConfigFactory.load()

  def configBoolean(key: String, defaultReturnValue: Boolean = false): Boolean = configValue[Boolean](key, conf.getBoolean).getOrElse(defaultReturnValue)
  def configBooleanList(key: String): List[Boolean]                            = configListValues[Boolean](key, conf.getBooleanList).getOrElse(List())
  def globalConfigBoolean(key: String, defaultReturnValue: Boolean = false): Boolean = {
    globalConfigValue[Boolean](key, (key: String) => key.toBoolean, conf.getBoolean).getOrElse(defaultReturnValue)
  }
  def globalConfigBooleanList(key: String, defaultReturnValue: List[Boolean] = List()): List[Boolean] = {
    globalConfigListValue[Boolean](
      key,
      (key: String) => decode[List[Boolean]](key).getOrElse(List()),
      conf.getBooleanList
    ).getOrElse(defaultReturnValue)
  }

  def configString(key: String, defaultReturnValue: String = ""): String = configValue[String](key, conf.getString).getOrElse(defaultReturnValue)
  def configStringList(key: String): List[String]                        = configListValues[String](key, conf.getStringList).getOrElse(List())
  def globalConfigString(key: String, defaultReturnValue: String = ""): String = {
    globalConfigValue[String](key, (key: String) => key, conf.getString).getOrElse(defaultReturnValue)
  }
  def globalConfigStringOption(key: String, defaultReturnValue: Option[String] = None): Option[String] = {
    globalConfigValue[Option[String]](
      key,
      (key: String) => Option(key),
      (key: String) => {
        try Some(conf.getString(key)).filterNot(_.isEmpty)
        catch {
          case _: Exception => None
        }
      }
    ).getOrElse(defaultReturnValue)
  }
  def globalConfigStringList(key: String, defaultReturnValue: List[String] = List()): List[String] = {
    globalConfigListValue[String](
      key,
      (key: String) => {
        val result = decode[List[String]](key)
        result.getOrElse(List())
      },
      conf.getStringList
    ).getOrElse(defaultReturnValue)
  }

  def configInt(key: String, defaultReturnValue: Int = 0): Int = configValue[Int](key, conf.getInt).getOrElse(defaultReturnValue)
  def configIntList(key: String): List[Int]                    = configListValues[Int](key, conf.getIntList).getOrElse(List())
  def globalConfigInt(key: String, defaultReturnValue: Int = 0): Int = {
    globalConfigValue[Int](key, (key: String) => key.toInt, conf.getInt).getOrElse(defaultReturnValue)
  }
  def globalConfigIntList(key: String, defaultReturnValue: List[Int] = List()): List[Int] = {
    globalConfigListValue[Int](
      key,
      (key: String) => decode[List[Int]](key).getOrElse(List()),
      conf.getIntList
    ).getOrElse(defaultReturnValue)
  }

  def configLong(key: String, defaultReturnValue: Long = 0): Long = configValue[Long](key, conf.getLong).getOrElse(defaultReturnValue)
  def configLongList(key: String): List[Long]                     = configListValues[Long](key, conf.getLongList).getOrElse(List())
  def globalConfigLong(key: String, defaultReturnValue: Long = 0): Long =
    globalConfigValue[Long](key, (key: String) => key.toLong, conf.getLong).getOrElse(defaultReturnValue)
  def globalConfigLongList(key: String, defaultReturnValue: List[Long] = List()): List[Long] = {
    globalConfigListValue[Long](key, (key: String) => decode[List[Long]](key).getOrElse(List()), conf.getLongList).getOrElse(defaultReturnValue)
  }

  def configDouble(key: String, defaultReturnValue: Double = 0.0): Double = configValue[Double](key, conf.getDouble).getOrElse(defaultReturnValue)
  def configDoubleList(key: String): List[Double]                         = configListValues[Double](key, conf.getDoubleList).getOrElse(List())
  def globalConfigDouble(key: String, defaultReturnValue: Double = 0): Double = {
    globalConfigValue[Double](key, (key: String) => key.toDouble, conf.getDouble).getOrElse(defaultReturnValue)
  }
  def globalConfigDoubleList(key: String, defaultReturnValue: List[Double] = List()): List[Double] =
    globalConfigListValue[Double](key, (key: String) => decode[List[Double]](key).getOrElse(List()), conf.getDoubleList).getOrElse(defaultReturnValue)

  def configDuration(key: String, defaultReturnValue: Duration = Duration.ZERO): Duration = {
    configValue[Duration](key, conf.getDuration).getOrElse(defaultReturnValue)
  }
  def configDurationList(key: String): List[Duration] = configListValues[Duration](key, conf.getDurationList).getOrElse(List())
  def globalConfigDuration(key: String, defaultReturnValue: Duration = Duration.ZERO): Duration = {
    globalConfigValue[Duration](key, (key: String) => Duration.ofNanos(ScalaDuration(key).toNanos), conf.getDuration).getOrElse(defaultReturnValue)
  }
  def globalConfigDurationList(key: String, defaultReturnValue: List[Duration] = List()): List[Duration] = {
    globalConfigListValue[Duration](
      key,
      (key: String) =>
        decode[List[String]](key)
          .getOrElse(List())
          .map(durationString => Duration.ofNanos(ScalaDuration(durationString).toNanos)),
      conf.getDurationList
    ).getOrElse(defaultReturnValue)
  }

  def getEnvValueStringOption(key: String): Option[String] = loadEnvValue(key, (key: String) => key)

  private def configValue[E <: Any](key: String, f: String => E): Option[E] = {
    val result = f(key)
    Some(result)
  }

  private def configListValues[E <: Any](key: String, f: String => java.util.List[_]): Option[List[E]] = {
    val result = f(key).asScala.toList.asInstanceOf[List[E]]
    Some(result)
  }

  private def globalConfigValue[E <: Any](key: String, castStringToValue: String => E, loadFromConfigValue: String => E): Option[E] = {
    val systemSettingKey = key.toUpperCase().replace(".", "_")
    val envResult        = loadEnvValue(systemSettingKey, castStringToValue = castStringToValue)
    if (envResult.isDefined) {
      envResult
    }
    else {
      configValue[E](key, loadFromConfigValue)
    }
  }

  private def globalConfigListValue[E <: Any](
      key: String,
      castStringToValue: String => List[E],
      loadFromConfigValue: String => java.util.List[_]
  ): Option[List[E]] = {
    val systemSettingKey           = key.toUpperCase().replace(".", "_")
    val envResult: Option[List[E]] = loadEnvValue(systemSettingKey, castStringToValue = castStringToValue)
    if (envResult.isDefined) {
      envResult
    }
    else {
      configListValues[E](key, loadFromConfigValue)
    }
  }

  private def loadEnvValue[E <: Any](systemSettingKey: String, castStringToValue: String => E): Option[E] = {
    val envSetting = System.getenv(systemSettingKey)
    if (envSetting != null && !"".equalsIgnoreCase(envSetting.trim)) {
      Some(castStringToValue(envSetting))
    }
    else {
      val propertySetting = System.getProperty(systemSettingKey)
      if (propertySetting != null && !"".equalsIgnoreCase(propertySetting.trim)) {
        Some(castStringToValue(propertySetting))
      }
      else {
        None
      }
    }
  }

}
