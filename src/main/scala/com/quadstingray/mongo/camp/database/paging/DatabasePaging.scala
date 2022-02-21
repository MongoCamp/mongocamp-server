package com.quadstingray.mongo.camp.database.paging
import com.sfxcode.nosql.mongo.MongoDAO

trait DatabasePaging[A <: Any] {

  def dao: MongoDAO[A]

  def paginate(rows: Long, page: Long): DatabasePaginationResult[A]

  def countResult: Long

}
