package dev.mongocamp.server.model

/** The numbers of a collection. `available` is false for a collection that has no statistics (a view), the numbers are 0 then. */
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
