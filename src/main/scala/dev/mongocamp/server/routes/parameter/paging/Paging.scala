package dev.mongocamp.server.routes.parameter.paging

case class Paging(rowsPerPage: Option[Long], page: Option[Long])
