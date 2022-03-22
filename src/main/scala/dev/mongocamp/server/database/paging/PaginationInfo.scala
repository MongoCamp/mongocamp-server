package dev.mongocamp.server.database.paging

case class PaginationInfo(allCount: Long, perPage: Long, page: Long, pagesCount: Long)
