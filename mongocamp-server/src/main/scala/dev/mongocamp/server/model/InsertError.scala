package dev.mongocamp.server.model

/** A document of an insert many request that was not stored: `index` is the position in the request, `code` the error code of the database. */
case class InsertError(index: Int, code: Int, message: String)
