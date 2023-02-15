package dev.mongocamp.micrometer.mongodb

import com.github.blemale.scaffeine.Scaffeine
import com.typesafe.config.{Config, ConfigFactory}
import org.mongodb.scala.{Document, MongoDatabase}

import scala.concurrent.duration._

private [mongodb] object MetricsCache {

  private [mongodb] val metricsCache = Scaffeine().recordStats().expireAfterWrite(expireAfterTime).build[String, Document]()

  private lazy val expireAfterTime = {
    val confValue = conf.getDuration("dev.mongocamp.micrometer.mongodb.step")
    if (confValue.toSeconds > 45.seconds) {
      30.seconds
    } else {
      (confValue.getSeconds * .75).seconds
    }
  }

  private lazy val conf: Config = ConfigFactory.load()

}
