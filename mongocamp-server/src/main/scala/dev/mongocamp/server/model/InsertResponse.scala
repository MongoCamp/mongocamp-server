package dev.mongocamp.server.model

/** `insertedCount` and `errors` are only filled for the insert of many documents: with `ordered=false` the documents that could be stored are stored and the
  * others are listed in `errors`, so that a retry can send exactly these.
  */
case class InsertResponse(wasAcknowledged: Boolean, insertedIds: List[String], insertedCount: Option[Long] = None, errors: Option[List[InsertError]] = None)
