package com.quadstingray.mongo.camp.database.paging
import com.quadstingray.mongo.camp.routes.parameter.paging.{ Paging, PagingFunctions }

case class PaginationResult[A <: Any](databaseObjects: List[A], paginationInfo: PaginationInfo)

object PaginationResult {

  def listToPaginationResult[A <: Any](list: List[A], pagingInfo: Paging): (List[A], PaginationInfo) = {
    val rowsPerPage = pagingInfo.rowsPerPage.getOrElse(PagingFunctions.DefaultRowsPerPage)
    val page        = pagingInfo.page.getOrElse(1L)
    val count       = list.size
    val allPages    = Math.ceil(count.toDouble / rowsPerPage).toInt
    val fromList    = list.slice(((page - 1) * rowsPerPage).toInt, (page * rowsPerPage).toInt)
    (fromList, PaginationInfo(count, rowsPerPage, page, allPages))
  }

}
