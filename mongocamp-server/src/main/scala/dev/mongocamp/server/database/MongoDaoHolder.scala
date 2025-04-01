package dev.mongocamp.server.database

object MongoDaoHolder {
  lazy val userDao: UserDao             = UserDao()
  lazy val rolesDao: RolesDao           = RolesDao()
  lazy val tokenCacheDao: TokenCacheDao = TokenCacheDao()
}
