package com.quadstingray.mongo.camp.routes.parameter.paging

import com.quadstingray.mongo.camp.routes.parameter.paging.PagingFunctions._
import sttp.tapir.{ header, query }

trait PagingFunctions {

  val pagingParameter = query[Option[Int]]("rowsPerPage")
    .example(Some(DefaultRowsPerPage))
    .description("Count elements per page")
    .and(query[Option[Int]]("page").example(Some(1)).description("Requested page of the ResultSets"))
    .mapTo[Paging]

  val pagingHeaderOutput = header[Int](HeaderPaginationCountRows)
    .example(200)
    .description("count all elements")
    .and(header[Int](HeaderPaginationPerPage).example(20).description("Count elements per page"))
    .and(header[Int](HeaderPaginationPage).example(1).description("Current page"))
    .and(header[Int](HeaderPaginationPagesCount).example(10).description("Count pages"))
    .mapTo[RecordPaginationInfo]

}

object PagingFunctions {
  final lazy val HeaderPaginationPerPage    = "x-pagination-rows-per-page"
  final lazy val HeaderPaginationPage       = "x-pagination-current-page"
  final lazy val HeaderPaginationCountRows  = "x-pagination-count-rows"
  final lazy val HeaderPaginationPagesCount = "x-pagination-count-pages"
  final lazy val DefaultRowsPerPage: Int    = 10
}
