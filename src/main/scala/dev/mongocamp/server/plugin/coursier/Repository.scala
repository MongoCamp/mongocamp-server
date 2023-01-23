package dev.mongocamp.server.plugin.coursier

import coursier.core.Authentication
import coursier.ivy.IvyRepository
import dev.mongocamp.server.exception.{ErrorCodes, MongoCampException}
import sttp.model.StatusCode

import scala.collection.mutable.ArrayBuffer

object Repository {
  def mvn(repoString: String): coursier.MavenRepository = {
    if (repoString.contains("@")) {
      val parts      = ArrayBuffer[String]() ++ repoString.split(':')
      val authString = parts.last
      parts.remove(parts.indexOf(authString))
      val url  = parts.mkString(":")
      val auth = authString.split('@')
      if (auth.length != 2) {
        throw new MongoCampException(
          s"Invalid Format of MVN Repository Configuration: $repoString. More Information at: https://server.mongocamp.dev/config/properties/plugins-mvn.html",
          StatusCode.PreconditionFailed,
          ErrorCodes.mvnRepositoryConfigurationInvalid
        )
      }
      coursier.MavenRepository(url, Some(Authentication(auth.head, auth.last)))
    }
    else {
      coursier.MavenRepository(repoString)
    }
  }

}
