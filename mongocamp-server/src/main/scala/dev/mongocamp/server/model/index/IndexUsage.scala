package dev.mongocamp.server.model.index

import java.util.Date

/** How often an index was used since the database server started to count (`ops`, `since`), without the address of the server that the database reports. */
case class IndexUsage(name: String, key: Map[String, Any], ops: Long, since: Date)
