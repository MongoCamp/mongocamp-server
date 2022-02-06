package com.quadstingray.mongo.rest.exception

import sttp.model.StatusCode

case class MongoRestException(message: String, statusCode: StatusCode, errorCode: Int = -1, additionalInfo: String = "") extends Exception {
  override def getMessage: String = message
}
