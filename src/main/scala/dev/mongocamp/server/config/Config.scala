package dev.mongocamp.server.config

import com.typesafe.config
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import io.circe.parser._

import java.time.Duration
import scala.concurrent.duration.{ Duration => ScalaDuration }
import scala.jdk.CollectionConverters._

private[config] trait Config extends LazyLogging {

  private lazy val conf: config.Config = ConfigFactory.load()

  def globalConfigBoolean(key: String): Boolean = {
    globalConfigValue[Boolean](key, (key: String) => key.toBoolean, conf.getBoolean).getOrElse(false)
  }
  def globalConfigBooleanList(key: String): List[Boolean] = {
    globalConfigListValue[Boolean](
      key,
      (key: String) => decode[List[Boolean]](key).getOrElse(List()),
      conf.getBooleanList
    ).getOrElse(List())
  }

  def globalConfigString(key: String): String = {
    globalConfigValue[String](key, (key: String) => key, conf.getString).getOrElse("")
  }
  def globalConfigStringOption(key: String): Option[String] = {
    globalConfigValue[Option[String]](
      key,
      (key: String) => Option(key),
      (key: String) => {
        try Some(conf.getString(key)).filterNot(_.isEmpty)
        catch {
          case _: Exception => None
        }
      }
    ).flatten
  }
  def globalConfigStringList(key: String): List[String] = {
    globalConfigListValue[String](
      key,
      (key: String) => {
        val result = decode[List[String]](key)
        result.getOrElse(List())
      },
      conf.getStringList
    ).getOrElse(List())
  }

  def globalConfigInt(key: String): Int = {
    globalConfigValue[Int](key, (key: String) => key.toInt, conf.getInt).getOrElse(0)
  }
  def globalConfigIntList(key: String): List[Int] = {
    globalConfigListValue[Int](
      key,
      (key: String) => decode[List[Int]](key).getOrElse(List()),
      conf.getIntList
    ).getOrElse(List())
  }

  def globalConfigLong(key: String): Long = globalConfigValue[Long](key, (key: String) => key.toLong, conf.getLong).getOrElse(0)
  def globalConfigLongList(key: String): List[Long] = {
    globalConfigListValue[Long](key, (key: String) => decode[List[Long]](key).getOrElse(List()), conf.getLongList).getOrElse(List())
  }

  def globalConfigDouble(key: String): Double = {
    globalConfigValue[Double](key, (key: String) => key.toDouble, conf.getDouble).getOrElse(0)
  }
  def globalConfigDoubleList(key: String): List[Double] =
    globalConfigListValue[Double](key, (key: String) => decode[List[Double]](key).getOrElse(List()), conf.getDoubleList).getOrElse(List())

  def globalConfigDuration(key: String): Duration = {
    globalConfigValue[Duration](key, (key: String) => Duration.ofNanos(ScalaDuration(key).toNanos), conf.getDuration).getOrElse(Duration.ZERO)
  }
  def globalConfigDurationList(key: String): List[Duration] = {
    globalConfigListValue[Duration](
      key,
      (key: String) =>
        decode[List[String]](key)
          .getOrElse(List())
          .map(durationString => Duration.ofNanos(ScalaDuration(durationString).toNanos)),
      conf.getDurationList
    ).getOrElse(List())
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
