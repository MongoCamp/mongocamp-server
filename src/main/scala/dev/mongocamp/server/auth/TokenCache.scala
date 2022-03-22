package dev.mongocamp.server.auth

import com.github.blemale.scaffeine.Scaffeine
import com.sfxcode.nosql.mongo._
import dev.mongocamp.server.auth.AuthHolder.{ configBoolean, globalConfigDuration, handler }
import dev.mongocamp.server.database.MongoDatabase.tokenCacheDao
import dev.mongocamp.server.model.auth.{ TokenCacheElement, UserInformation }
import org.joda.time.DateTime

import java.time.Duration
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.{ DurationInt, FiniteDuration }

object TokenCache {
  val keyToken   = "token"
  val keyValidTo = "validTo"

  if (configBoolean("auth.cache.db")) {
    tokenCacheDao.createUniqueIndexForField(keyToken).result()
    tokenCacheDao.createExpiringIndexForField(keyValidTo, 1.seconds).result()
  }

  private lazy val cacheDuration: FiniteDuration = {
    if (configBoolean("auth.cache.db")) {
      FiniteDuration(5, TimeUnit.MINUTES)
    }
    else {
      FiniteDuration(expiringDuration.getSeconds, TimeUnit.SECONDS)
    }
  }

  private lazy val internalTokenCache = Scaffeine().recordStats().expireAfterWrite(cacheDuration).build[String, UserInformation]()
  lazy val expiringDuration: Duration = globalConfigDuration("auth.expiringDuration")

  def validateToken(token: String): Option[UserInformation] = {
    val cachedToken = internalTokenCache.getIfPresent(token)
    if (cachedToken.isEmpty && configBoolean("auth.cache.db")) {
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
    val element = TokenCacheElement(token, userInformation.userId, new DateTime().plusSeconds(expiringDuration.getSeconds.toInt).toDate)
    if (configBoolean("auth.cache.db")) {
      tokenCacheDao.insertOne(element).asFuture()
    }
  }
}
