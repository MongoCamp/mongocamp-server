package dev.mongocamp.server.model

/** The result of an import: `read` lines with a document were read, `inserted` are stored, `failed` are not. `errors` names the line (1 based, blank lines are
  * counted) and the reason, `index` of an [[InsertError]] is the number of the line. Only the first errors are listed (`errorsTruncated`), `failed` is the
  * number of all.
  */
case class ImportResponse(read: Long, inserted: Long, failed: Long, errors: List[InsertError], errorsTruncated: Boolean)
