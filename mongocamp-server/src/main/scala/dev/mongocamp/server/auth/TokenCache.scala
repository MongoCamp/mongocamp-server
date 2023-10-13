package dev.mongocamp.server.auth

import com.github.blemale.scaffeine.Scaffeine
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.auth.AuthHolder.handler
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.database.MongoDaoHolder.tokenCacheDao
import dev.mongocamp.server.jobs.CleanUpTokenJob
import dev.mongocamp.server.model.JobConfig
import dev.mongocamp.server.model.auth.UserInformation
import dev.mongocamp.server.plugin.JobPlugin
import dev.mongocamp.server.service.ConfigurationService
import org.joda.time.DateTime

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.{ Duration, DurationInt, FiniteDuration }
import scala.util.Try

object TokenCache {
  val keyToken   = "token"
  val keyValidTo = "validTo"

  private lazy val internalTokenCache = Scaffeine().recordStats().expireAfterWrite(cacheDuration).build[String, UserInformation]()

  Try {
    val cleanUpJobClass = classOf[CleanUpTokenJob]
    JobPlugin.addJob(JobConfig(cleanUpJobClass.getSimpleName, cleanUpJobClass.getName, "", "0 0/5 * ? * * *", "CleanUp", 10))
  }
  private def authTokenCacheDB = ConfigurationService.getConfigValue[Boolean](DefaultConfigurations.ConfigKeyAuthCacheDb)

  if (authTokenCacheDB) {
    tokenCacheDao.createUniqueIndexForField(keyToken).result()
    tokenCacheDao.createExpiringIndexForField(keyValidTo, 1.seconds).result()
  }

  def cacheDuration: FiniteDuration = {
    if (authTokenCacheDB) {
      FiniteDuration(5, TimeUnit.MINUTES)
    }
    else {
      val duration = ConfigurationService.getConfigValue[Duration](DefaultConfigurations.ConfigKeyAuthExpiringDuration)
      FiniteDuration(duration.toNanos, TimeUnit.NANOSECONDS)
    }
  }

  def validateToken(token: String): Option[UserInformation] = {
    val cachedToken = internalTokenCache.getIfPresent(token)
    if (cachedToken.isEmpty && authTokenCacheDB) {
      val userOption = tokenCacheDao
        .find(Map(keyToken -> token))
        .resultOption()
        .filter(tokenCache => {
          val result = new DateTime().isBefore(new DateTime(tokenCache.validTo))
          if (!result) {
            tokenCacheDao.deleteOne(Map(keyToken -> tokenCache.token)).asFuture()
          }
          result
        })
        .map(tokenCache => handler.findUser(tokenCache.userId))
      if (userOption.isDefined) {
        internalTokenCache.put(token, userOption.get)
      }
      userOption
    }
    else {
      cachedToken
    }
  }

  def invalidateToken(token: String): Unit = {
    internalTokenCache.invalidate(token)
    tokenCacheDao.deleteMany(Map(keyToken -> token)).resultOption()
  }

  def saveToken(token: String, userInformation: UserInformation): Unit = {
    internalTokenCache.put(token, userInformation)
    val element = dev.mongocamp.server.model.auth.TokenCacheElement(
      token,
      userInformation.userId,
      new DateTime().plusMillis(ConfigurationService.getConfigValue[Duration](DefaultConfigurations.ConfigKeyAuthExpiringDuration).toMillis.toInt).toDate
    )
    if (authTokenCacheDB) {
      tokenCacheDao.insertOne(element).asFuture()
    }
  }
}
