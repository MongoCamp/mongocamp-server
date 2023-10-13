package dev.mongocamp.server.plugin.coursier

import coursier.core.Authentication
import dev.mongocamp.server.exception.{ ErrorCodes, MongoCampException }
import sttp.model.StatusCode

import scala.collection.mutable.ArrayBuffer

object Repository {
  def mvn(repoString: String): coursier.MavenRepository = {
    val repoAuthSplitter = "@"
    val authSplitter     = ':'
    if (repoString.contains(repoAuthSplitter) || !repoString.startsWith("htt")) {
      val parts      = ArrayBuffer[String]() ++ repoString.split(repoAuthSplitter)
      val authString = parts.head
      parts.remove(parts.indexOf(authString))
      val url  = parts.mkString(repoAuthSplitter)
      val auth = authString.split(authSplitter)
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
