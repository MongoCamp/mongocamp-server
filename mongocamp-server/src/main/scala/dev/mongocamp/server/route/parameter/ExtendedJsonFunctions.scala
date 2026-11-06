package dev.mongocamp.server.route.parameter

import sttp.tapir._

object ExtendedJsonFunctions {

  /** The query parameter `extendedJson` of the routes that return documents, see [[dev.mongocamp.server.converter.ExtendedJsonWriter]]. */
  val extendedJsonParameter: EndpointInput.Query[Option[String]] =
    query[Option[String]]("extendedJson")
      .description(
        "relaxed or canonical: the documents with the types of their values as MongoDB Extended JSON ({\"$oid\": ...}, {\"$date\": ...}, {\"$numberDecimal\": ...}, canonical also {\"$numberInt\": ...}, {\"$numberLong\": ...} and {\"$numberDouble\": ...}). Without the parameter the documents are returned as before."
      )
      .default(None)
}
