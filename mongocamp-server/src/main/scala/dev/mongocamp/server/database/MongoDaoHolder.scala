package dev.mongocamp.server.database

import dev.mongocamp.server.model.auth.{ Grant, Role, TokenCacheElement, UserInformation }
import dev.mongocamp.server.model.{ DBFileInformation, JobConfig }
import org.mongodb.scala.bson.codecs.Macros._

object MongoDaoHolder {
  lazy val userDao: UserDao             = UserDao()
  lazy val rolesDao: RolesDao           = RolesDao()
  lazy val tokenCacheDao: TokenCacheDao = TokenCacheDao()
  lazy val jobDao: JobDao               = JobDao()

  MongoDatabase.addToProvider(classOf[UserInformation])
  MongoDatabase.addToProvider(classOf[Role])
  MongoDatabase.addToProvider(classOf[Grant])
  MongoDatabase.addToProvider(classOf[TokenCacheElement])
  MongoDatabase.addToProvider(classOf[DBFileInformation])
  MongoDatabase.addToProvider(classOf[JobConfig])
}
