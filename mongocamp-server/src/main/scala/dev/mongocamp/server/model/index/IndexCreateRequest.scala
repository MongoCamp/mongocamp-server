package dev.mongocamp.server.model.index

/** The keys of the index: `keys` is the map of the older versions (field to 1 or -1, no reliable order for more than a few keys), `orderedKeys` is a list in
  * the order of the index and also allows the types of keys (text, 2dsphere, 2d, hashed). Send one of both.
  */
case class IndexCreateRequest(
  keys: Option[Map[String, Int]] = None,
  indexOptionsRequest: Option[IndexOptionsRequest] = None,
  orderedKeys: Option[List[IndexKey]] = None
)
