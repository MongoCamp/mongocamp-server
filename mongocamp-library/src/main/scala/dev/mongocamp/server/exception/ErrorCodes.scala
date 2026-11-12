package dev.mongocamp.server.exception

object ErrorCodes {
  val emptyFilter: Int              = 4003
  val emptyBatch: Int               = 4002
  val ambiguousFilter: Int          = 4004
  val filterNotSupported: Int       = 4005
  val invalidIndexKey: Int          = 4006
  val invalidCollectionName: Int    = 4007
  val collectionExists: Int         = 4091
  val invalidImportLine: Int        = 4008
  val invalidCollectionOptions: Int = 4009
  val badInsertRequest: Int         = 4401
  val idMissingForReplace: Int      = 4402

  val authMethodNotImplemented: Int          = 5001
  val mvnRepositoryConfigurationInvalid: Int = 5002

  val unauthorizedUser: Int             = 4011
  val unauthorizedUserForOtherUser: Int = 4012

  val forbiddenStage: Int             = 4031
  val forbiddenCollectionInStage: Int = 4032

  val jobAlreadyAdded: Int    = 4121
  val jobCouldNotUpdated: Int = 4122
  val jobCouldNotFound: Int   = 4041
  val jobClassNotFound: Int   = 4042

}
