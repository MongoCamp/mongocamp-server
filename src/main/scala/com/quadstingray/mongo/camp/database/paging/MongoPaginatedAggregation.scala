package com.quadstingray.mongo.camp.database.paging

import com.mongodb.client.model.Facet
import com.quadstingray.mongo.camp.exception.MongoCampException
import com.sfxcode.nosql.mongo.{ MongoDAO, _ }
import org.mongodb.scala.bson.Document
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Aggregates
import sttp.model.StatusCode

import scala.jdk.CollectionConverters._

case class MongoPaginatedAggregation[A <: Any](
    dao: MongoDAO[A],
    allowDiskUse: Boolean = false,
    aggregationPipeline: List[Bson] = List(),
    castDocumentToValue: Document => A
) extends DatabasePaging[A] {

  private val AggregationKeyMetaData      = "metadata"
  private val AggregationKeyData          = "data"
  private val AggregationKeyMetaDataTotal = "total"

  override def paginate(rows: Long, page: Long): DatabasePaginationResult[A] = {
    if (rows <= 0) {
      throw MongoCampException("rows per page must be greater then 0.", StatusCode.BadRequest)
    }
    if (page <= 0) {
      throw MongoCampException("page must be greater then 0.", StatusCode.BadRequest)
    }

    val skip = (page - 1) * rows

    val listOfMetaData: List[Bson] = List(Map("$count" -> AggregationKeyMetaDataTotal))
    val listOfPaging: List[Bson]   = List(Map("$skip" -> skip), Map("$limit" -> rows))

    val pipeline =
      aggregationPipeline ++ List(
        Aggregates.facet(new Facet(AggregationKeyMetaData, listOfMetaData.asJava), new Facet(AggregationKeyData, listOfPaging.asJava))
      )

    val dbResponse = dao.findAggregated(pipeline, allowDiskUse).result().asInstanceOf[Document]

    val count: Long = dbResponse.get(AggregationKeyMetaData).get.asArray().get(0).asDocument().get(AggregationKeyMetaDataTotal).asNumber().longValue()
    val allPages    = Math.ceil(count.toDouble / rows).toInt
    val list        = dbResponse.get("data").get.asArray().asScala.map(_.asDocument()).map(document => castDocumentToValue(document))
    DatabasePaginationResult(list.toList, PaginationInfo(count, rows, page, allPages))
  }

  override def countResult: Long = {
    val listOfMetaData: List[Bson] = List(Map("$count" -> AggregationKeyMetaDataTotal))
    val listOfPaging: List[Bson]   = List(Map("$skip" -> 0), Map("$limit" -> 1))

    val pipeline = aggregationPipeline ++ List(
      Aggregates.facet(new Facet(AggregationKeyMetaData, listOfMetaData.asJava), new Facet(AggregationKeyData, listOfPaging.asJava))
    )
    val dbResponse  = dao.findAggregated(pipeline, allowDiskUse).result().asInstanceOf[Document]
    val count: Long = dbResponse.get(AggregationKeyMetaData).get.asArray().get(0).asDocument().get(AggregationKeyMetaDataTotal).asNumber().longValue()
    count
  }

}
