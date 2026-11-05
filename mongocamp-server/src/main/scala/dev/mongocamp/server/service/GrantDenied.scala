package dev.mongocamp.server.service

import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.exception.ErrorCodes
import dev.mongocamp.server.exception.MongoCampException
import sttp.model.StatusCode

/** The error for a user who is logged in but has no grant for what was requested. The status is configured (`AUTH_GRANT_DENIED_STATUS`): `401` is the historic
  * answer, `403` does not make a client clear its session. The error code and the message are the same for both.
  */
object GrantDenied {

  def status: StatusCode =
    if (ConfigurationService.getConfigValue[Long](DefaultConfigurations.ConfigKeyAuthGrantDeniedStatus) == 403L) StatusCode.Forbidden
    else StatusCode.Unauthorized

  def exception(message: String): MongoCampException = MongoCampException(message, status, ErrorCodes.unauthorizedUser)
}
