package dev.mongocamp.server.exception
import dev.mongocamp.server.converter.CirceSchema
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
    .and(header[String](HeaderErrorMessage).example("Error Message").description("Message of the MongoCampException"))
    .and(header[String](HeaderErrorAdditionalInfo).example("Additional Error Info").description("Additional information for the MongoCampException"))
    .mapTo[ErrorDescription]

  final lazy val errorEndpointDefinition = statusCode.and(jsonBody[ErrorDescription]).and(errorHeaderParameter)

}
