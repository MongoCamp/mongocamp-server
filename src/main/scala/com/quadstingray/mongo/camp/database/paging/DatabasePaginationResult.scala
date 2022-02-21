package com.quadstingray.mongo.camp.database.paging

case class DatabasePaginationResult[A <: Any](databaseObjects: List[A], paginationInfo: PaginationInfo)
