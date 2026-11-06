package dev.mongocamp.server.model.index

/** A key of an index. `direction` is `1` or `-1` for a sorted key, otherwise the type of the key: `text`, `2dsphere`, `2d`, `hashed`, `wildcard`. */
case class IndexKey(field: String, direction: String)

/** An index with everything that the database reports: the keys in the order of the index, the options and the weights of a text index. */
case class IndexInfo(
  name: String,
  keys: List[IndexKey],
  unique: Boolean,
  sparse: Boolean,
  hidden: Boolean,
  partialFilterExpression: Option[Map[String, Any]],
  collation: Option[Map[String, Any]],
  weights: Option[Map[String, Any]],
  defaultLanguage: Option[String],
  expireAfterSeconds: Option[Long],
  version: Int
)
