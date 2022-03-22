package dev.mongocamp.server.exception

case class ErrorDescription(
    code: Int,
    msg: String = "",
    additionalInfo: String = ""
)
