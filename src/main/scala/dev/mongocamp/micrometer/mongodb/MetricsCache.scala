package dev.mongocamp.micrometer.mongodb

import com.github.blemale.scaffeine.Scaffeine
import org.mongodb.scala.{Document, MongoDatabase}

import scala.concurrent.duration.DurationInt

object MetricsCache {
  val metricsCache = Scaffeine().recordStats().expireAfterWrite(30.seconds).build[String, Document]()
}
