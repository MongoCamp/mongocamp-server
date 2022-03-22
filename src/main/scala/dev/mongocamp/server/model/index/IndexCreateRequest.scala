package dev.mongocamp.server.model.index

case class IndexCreateRequest(keys: Map[String, Int], indexOptionsRequest: Option[IndexOptionsRequest])
