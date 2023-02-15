package dev.mongocamp.micrometer.mongodb

import com.github.blemale.scaffeine.{Cache, Scaffeine}
import com.typesafe.config.{Config, ConfigFactory}
import org.mongodb.scala.Document

import java.time
import scala.concurrent.duration._

private[mongodb] object MetricsCache {

  private var expireAfterTime = {
    val confValue = conf.getDuration("dev.mongocamp.micrometer.mongodb.step")
    calculateExpireAfterTime(confValue)
  }

  private var metricsCache: Cache[String, Document] = createCache()

  private def calculateExpireAfterTime(confValue: time.Duration): FiniteDuration = {
    if (confValue.toSeconds.seconds > 45.seconds) {
      30.seconds
    }
    else {
      var cacheSeconds = confValue.getSeconds * .75
      if (cacheSeconds < 1) {
        cacheSeconds = 1
      }
      cacheSeconds.seconds
    }
  }

  private lazy val conf: Config = ConfigFactory.load()

  private def createCache(): Cache[String, Document] = {
    Scaffeine().recordStats().expireAfterWrite(expireAfterTime).build[String, Document]()
  }

  def getMetricsCache: Cache[String, Document] = metricsCache
  def updateCacheTime(value: time.Duration): Unit = {
    val newExpireAfterTime = calculateExpireAfterTime(time.Duration.ofMillis(value.toMillis))
    if (newExpireAfterTime < expireAfterTime) {
      expireAfterTime = newExpireAfterTime
      metricsCache = createCache()
    }
  }
}
