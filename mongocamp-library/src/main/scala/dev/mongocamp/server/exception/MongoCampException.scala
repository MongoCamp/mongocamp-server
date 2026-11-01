package dev.mongocamp.server.exception

import sttp.model.StatusCode

case class MongoCampException(message: String, statusCode: StatusCode, errorCode: Int = -1, additionalInfo: String = "") extends Exception {
  override def getMessage: String = message
}

object MongoCampException {

  def unauthorizedException(message: String = "user not authorized", errorCode: Int = ErrorCodes.unauthorizedUser): MongoCampException =
    MongoCampException(message, StatusCode.Unauthorized, errorCode)

  def badRequestException(message: String, errorCode: Int = -1): MongoCampException =
    MongoCampException(message, StatusCode.BadRequest, errorCode)

  // 403 and not 401: a client such as the UI clears its session on 401, but the session is fine, only this request is refused
  def forbiddenException(message: String, errorCode: Int = -1): MongoCampException =
    MongoCampException(message, StatusCode.Forbidden, errorCode)

  def badAuthConfiguration(): MongoCampException =
    MongoCampException("Authentication method not configured correctly", StatusCode.NotImplemented, ErrorCodes.authMethodNotImplemented)

  def userNotFoundException   = MongoCampException("user  does not exists", StatusCode.NotFound)
  def userOrPasswordException = MongoCampException("user or password does not exists", StatusCode.Unauthorized)
  def apiKeyException         = MongoCampException("apiKey not valid", StatusCode.Unauthorized)
}
