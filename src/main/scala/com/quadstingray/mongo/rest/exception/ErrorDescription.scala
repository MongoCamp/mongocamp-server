package com.quadstingray.mongo.rest.exception

case class ErrorDescription(
    code: Int,
    msg: String = "",
    additionalInfo: String = ""
)
