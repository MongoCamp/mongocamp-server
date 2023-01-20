package dev.mongocamp.micrometer.mongodb.registry

import better.files.File
import com.typesafe.config.{ Config, ConfigFactory }
import dev.mongocamp.driver.mongodb.MongoDAO
import io.micrometer.core.instrument.step.StepRegistryConfig
import org.mongodb.scala.Document

case class MonitoringMongoConfig(mongoDAO: MongoDAO[Document], configurationMap: Map[String, String] = Map()) extends StepRegistryConfig {

  private lazy val conf: Config = ConfigFactory.load()

  override def prefix(): String = "dev.mongocamp.micrometer.mongodb"

  override def get(key: String): String = {
    loadConfigValue(key).orNull
  }

  private def loadConfigValue(key: String): Option[String] = {
    try {
      val mapKey = key.replace(s"$prefix.", "")
      (configurationMap.get(mapKey) ++ configurationMap.get(key)).foreach(v => return Some(v))
      Option(conf.getValue(key).render().replace("\"", ""))
    }
    catch {
      case _: Exception =>
        None
    }
  }

}
