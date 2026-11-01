package dev.mongocamp.server.auth

import com.github.blemale.scaffeine.Scaffeine
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.auth.AuthHolder.handler
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.database.MongoDaoHolder.tokenCacheDao
import dev.mongocamp.server.jobs.CleanUpTokenJob
import dev.mongocamp.server.model.auth.UserInformation
import dev.mongocamp.server.model.JobConfig
import dev.mongocamp.server.plugin.JobPlugin
import dev.mongocamp.server.service.ConfigurationService
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import org.joda.time.DateTime
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters._

object TokenCache {
  val keyToken   = "token"
  val keyValidTo = "validTo"

  private lazy val internalTokenCache = Scaffeine().recordStats().expireAfterWrite(cacheDuration).build[String, UserInformation]()

  try {
    val cleanUpJobClass = classOf[CleanUpTokenJob]
    JobPlugin.addJob(JobConfig(cleanUpJobClass.getSimpleName, cleanUpJobClass.getName, "", "0 0/5 * ? * * *", "CleanUp", 10))
  }
  catch {
    case _: Throwable =>
      ""
  }
  // tokens that were replaced by a refresh: token -> epoch millis from which they are no longer valid
  private val supersededTokens = new ConcurrentHashMap[String, java.lang.Long]()

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
    if (isSuperseded(token)) None else validateKnownToken(token)
  }

  private def isSuperseded(token: String): Boolean = {
    Option(supersededTokens.get(token)).exists(
      revokeAt =>
        if (System.currentTimeMillis() >= revokeAt) {
          invalidateToken(token)
          true
        }
        else false
    )
  }

  private def validateKnownToken(token: String): Option[UserInformation] = {
    val cachedToken = internalTokenCache.getIfPresent(token)
    if (cachedToken.isEmpty && authTokenCacheDB) {
      val userOption = tokenCacheDao
        .find(Map(keyToken -> token))
        .resultOption()
        .filter(
          tokenCache => {
            val result = new DateTime().isBefore(new DateTime(tokenCache.validTo))
            if (!result) {
              tokenCacheDao.deleteOne(Map(keyToken -> tokenCache.token)).asFuture()
            }
            result
          }
        )
        .map(
          tokenCache => handler.findUser(tokenCache.userId)
        )
      if (userOption.isDefined) {
        internalTokenCache.put(token, userOption.get)
      }
      userOption
    }
    else {
      cachedToken
    }
  }

  /** The token stays valid for the grace period (requests that are already on the way, concurrent clients) and is invalid afterwards. */
  def supersede(token: String, grace: FiniteDuration): Unit = {
    val now      = System.currentTimeMillis()
    val revokeAt = now + grace.toMillis
    // tokens that are never presented again would stay in the map
    // (the token is invalidated, not only forgotten, otherwise it would be valid again)
    supersededTokens.entrySet().asScala.toList.filter(_.getValue.longValue() <= now).foreach(entry => invalidateToken(entry.getKey))
    supersededTokens.merge(token, revokeAt, (old, new_) => math.min(old.longValue(), new_.longValue()))
    if (authTokenCacheDB) {
      // the other instances read the token from the database when their local copy is gone
      tokenCacheDao.updateOne(Map(keyToken -> token, keyValidTo -> Map("$gt" -> new DateTime(revokeAt).toDate)), Map("$set" -> Map(keyValidTo -> new DateTime(revokeAt).toDate))).asFuture()
    }
  }

  def invalidateToken(token: String): Unit = {
    supersededTokens.remove(token)
    internalTokenCache.invalidate(token)
    tokenCacheDao.deleteMany(Map(keyToken -> token)).resultOption()
  }

  def saveToken(token: String, userInformation: UserInformation): Unit = {
    internalTokenCache.put(token, userInformation)
    val element = dev.mongocamp.server.model.auth.TokenCacheElement(
      token,
      userInformation.userId,
      // plusMillis takes an Int, which overflows for durations of more than about 24.8 days
      new DateTime(System.currentTimeMillis() + ConfigurationService.getConfigValue[Duration](DefaultConfigurations.ConfigKeyAuthExpiringDuration).toMillis)
    )
    if (authTokenCacheDB) {
      tokenCacheDao.insertOne(element).asFuture()
    }
  }
}
