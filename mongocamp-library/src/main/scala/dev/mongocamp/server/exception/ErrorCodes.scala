package dev.mongocamp.server.exception

object ErrorCodes {
  val badInsertRequest: Int    = 4401
  val idMissingForReplace: Int = 4402

  val authMethodNotImplemented: Int          = 5001
  val mvnRepositoryConfigurationInvalid: Int = 5002

  val unauthorizedUser: Int             = 4011
  val unauthorizedUserForOtherUser: Int = 4012

  val jobAlreadyAdded: Int    = 4121
  val jobCouldNotUpdated: Int = 4122
  val jobCouldNotFound: Int   = 4041
  val jobClassNotFound: Int   = 4042

}
