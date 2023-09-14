package dev.mongocamp.server.route.parameter.paging

case class Paging(rowsPerPage: Option[Long], page: Option[Long])
