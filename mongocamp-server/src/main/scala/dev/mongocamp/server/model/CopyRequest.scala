package dev.mongocamp.server.model

/** `filter` is a Lucene filter like in the list, only the matching documents are copied. `withIndexes` (default true) recreates the indices of the collection
  * for the copy. An existing target is a conflict unless `dropTarget` is true, then it is replaced when the copy is complete.
  */
case class CopyRequest(target: String, filter: Option[String] = None, withIndexes: Option[Boolean] = None, dropTarget: Option[Boolean] = None)

case class CopyResponse(name: String, count: Long, indexes: Int)
