package dev.mongocamp.server.model

/** The collation of a collection: the language rules for comparing and sorting text. */
case class CollectionCollation(locale: String, strength: Option[Int] = None, caseLevel: Option[Boolean] = None, numericOrdering: Option[Boolean] = None)

/** The options of a collection: the body of the creation (all optional), of the change of the validation (`validator`, `validationLevel`, `validationAction`
  * only) and the answer of the options of a collection. `validator` is a MongoDB query or `{"$jsonSchema": {...}}`, `validationLevel` is `off`, `moderate` or
  * `strict` (the database default is `strict`), `validationAction` is `error` or `warn` (the default is `error`).
  */
case class CollectionOptions(
  capped: Option[Boolean] = None,
  sizeInBytes: Option[Long] = None,
  maxDocuments: Option[Long] = None,
  validator: Option[Map[String, Any]] = None,
  validationLevel: Option[String] = None,
  validationAction: Option[String] = None,
  collation: Option[CollectionCollation] = None
)

/** The full name of the created collection (`name` in the default database, `database:name` in another). */
case class CreateCollectionResponse(name: String)
