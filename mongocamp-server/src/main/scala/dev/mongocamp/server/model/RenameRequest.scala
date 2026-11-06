package dev.mongocamp.server.model

/** `target` is the new name of the collection, in the database of the collection (`db:name` is allowed for the same database). An existing target is a conflict
  * unless `dropTarget` is true, then it is replaced.
  */
case class RenameRequest(target: String, dropTarget: Option[Boolean] = None)

case class RenameResponse(name: String)
