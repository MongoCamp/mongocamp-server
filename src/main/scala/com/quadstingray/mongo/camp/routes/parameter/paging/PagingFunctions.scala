package com.quadstingray.mongo.camp.routes.parameter.paging

import com.quadstingray.mongo.camp.database.paging.PaginationInfo
import sttp.tapir.{ header, query }

object PagingFunctions {

  val pagingParameter = query[Option[Long]]("rowsPerPage")
    .example(Some(DefaultRowsPerPage))
    .description("Count elements per page")
    .and(query[Option[Long]]("page").example(Some(1L)).description("Requested page of the ResultSets"))
    .mapTo[Paging]

  val pagingHeaderOutput = header[Long](HeaderPaginationCountRows)
    .example(200)
    .description("count all elements")
    .and(header[Long](HeaderPaginationPerPage).example(20).description("Count elements per page"))
    .and(header[Long](HeaderPaginationPage).example(1).description("Current page"))
    .and(header[Long](HeaderPaginationPagesCount).example(10).description("Count pages"))
    .mapTo[PaginationInfo]

  final lazy val HeaderPaginationPerPage    = "x-pagination-rows-per-page"
  final lazy val HeaderPaginationPage       = "x-pagination-current-page"
  final lazy val HeaderPaginationCountRows  = "x-pagination-count-rows"
  final lazy val HeaderPaginationPagesCount = "x-pagination-count-pages"
  final lazy val DefaultRowsPerPage: Long   = 10
}
