package com.quadstingray.mongo.camp.exception

object ErrorCodes {
  val badInsertRequest: Int    = 4401
  val idMissingForReplace: Int = 4402

  val authMethodNotImplemented: Int = 5001

  val unauthorizedUser: Int             = 4011
  val unauthorizedUserForOtherUser: Int = 4012

}
