package dev.mongocamp.server.model.auth

case class Grant(name: String, read: Boolean, write: Boolean, administrate: Boolean, grantType: String)

object Grant {
  val grantTypeCollection: String = "COLLECTION"
  val grantTypeBucket: String     = "BUCKET"
  val grantTypeBucketMeta: String = "BUCKET_META"
}
