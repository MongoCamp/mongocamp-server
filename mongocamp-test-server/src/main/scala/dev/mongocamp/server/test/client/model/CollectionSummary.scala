package dev.mongocamp.server.test.client.model

case class CollectionSummary(
  name: String,
  collectionType: String,
  count: Long,
  size: Double,
  storageSize: Double,
  avgObjSize: Long,
  nindexes: Int,
  totalIndexSize: Long,
  available: Boolean
)
