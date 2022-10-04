package dev.mongocamp.server.jobs

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.auth.TokenCache
import dev.mongocamp.server.auth.TokenCache.keyToken
import dev.mongocamp.server.config.{ConfigManager, DefaultConfigurations}
import dev.mongocamp.server.database.MongoDatabase.tokenCacheDao
import org.joda.time.DateTime
import org.mongodb.scala.model.Filters.lte
import org.quartz.{Job, JobExecutionContext}

class CleanUpTokenJob extends Job with LazyLogging {

  override def execute(context: JobExecutionContext): Unit = {
    if (ConfigManager.getConfigValue[Boolean](DefaultConfigurations.ConfigKeyAuthCacheDb)) {
      tokenCacheDao
        .find(lte(TokenCache.keyValidTo, new DateTime().toDate))
        .foreach(tokenCache => {
          val isValid = new DateTime().isBefore(new DateTime(tokenCache.validTo))
          if (!isValid) {
            tokenCacheDao.deleteOne(Map(keyToken -> tokenCache.token)).asFuture()
          }
        })
    }
  }

}
