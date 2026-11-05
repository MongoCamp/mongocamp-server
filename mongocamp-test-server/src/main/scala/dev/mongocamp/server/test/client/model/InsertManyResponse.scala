package dev.mongocamp.server.test.client.model

case class InsertError(index: Int, code: Int, message: String)

case class InsertManyResponse(
  wasAcknowledged: Boolean,
  insertedIds: Seq[String] = Seq.empty,
  insertedCount: Option[Long] = None,
  errors: Option[Seq[InsertError]] = None
)
