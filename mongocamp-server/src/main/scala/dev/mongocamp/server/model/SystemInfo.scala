package dev.mongocamp.server.model

/** Information about the system the server is connected to. `mongoDbVersion` is the version of the MongoDB server (`7.0.5`), `mongoDbVersionArray` the numbers
  * of it (`[7, 0, 5, 0]`).
  */
case class SystemInfo(mongoDbVersion: String, mongoDbVersionArray: List[Int])
