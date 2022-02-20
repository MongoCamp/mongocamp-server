package com.quadstingray.mongo.camp.exception

case class ErrorDescription(
    code: Int,
    msg: String = "",
    additionalInfo: String = ""
)
