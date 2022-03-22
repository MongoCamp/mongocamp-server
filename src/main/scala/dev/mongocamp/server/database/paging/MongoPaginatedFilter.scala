package dev.mongocamp.server.database.paging

import com.sfxcode.nosql.mongo.{ MongoDAO, _ }
import dev.mongocamp.server.exception.MongoCampException
import org.mongodb.scala.bson.conversions.Bson
import sttp.model.StatusCode

case class MongoPaginatedFilter[A <: Any](dao: MongoDAO[A], filter: Bson = Map(), sort: Bson = Map(), projection: Bson = Map()) {

  def paginate(rows: Long, page: Long): PaginationResult[A] = {
    val count = countResult
    if (rows <= 0) {
      throw MongoCampException("rows per page must be greater then 0.", StatusCode.BadRequest)
    }
    if (page <= 0) {
      throw MongoCampException("page must be greater then 0.", StatusCode.BadRequest)
    }
    val allPages     = Math.ceil(count.toDouble / rows).toInt
    val skip         = (page - 1) * rows
    val responseList = dao.find(filter, sort, projection, rows.toInt).skip(skip.toInt).resultList()
    PaginationResult(responseList, PaginationInfo(count, rows, page, allPages))
  }

  def countResult: Long = dao.count(filter).result()

}
