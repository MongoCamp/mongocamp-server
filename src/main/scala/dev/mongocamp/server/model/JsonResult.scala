package dev.mongocamp.server.model

case class JsonResult[A <: Any](value: A)
