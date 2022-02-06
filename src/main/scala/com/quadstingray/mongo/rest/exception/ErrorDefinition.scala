package com.quadstingray.mongo.rest.exception

import com.quadstingray.mongo.rest.converter.CirceSchema
import io.circe.generic.auto._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.{ header, statusCode }

object ErrorDefinition extends CirceSchema {

  final lazy val HeaderErrorMessage        = "x-error-message"
  final lazy val HeaderErrorCode           = "x-error-code"
  final lazy val HeaderErrorAdditionalInfo = "x-error-additional-info"

  final lazy val errorHeaderParameter = header[Int](HeaderErrorCode)
    .example(100)
    .description("Error Code")
    .and(header[String](HeaderErrorMessage).example("Error Message").description("Textuelle Fehlermeldung"))
    .and(header[String](HeaderErrorAdditionalInfo).example("Additional Error Info").description("Weitergehende Informationen zum Fehler"))
    .mapTo[ErrorDescription]

  final lazy val errorEndpointDefinition = statusCode.and(jsonBody[ErrorDescription]).and(errorHeaderParameter)

}
