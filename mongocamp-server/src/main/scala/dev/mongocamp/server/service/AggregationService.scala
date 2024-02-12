package dev.mongocamp.server.service
import dev.mongocamp.driver.mongodb.mapToBson
import dev.mongocamp.server.converter.MongoCampBsonConverter
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.database.paging.{ MongoPaginatedAggregation, PaginationInfo }
import dev.mongocamp.server.model.auth.AuthorizedCollectionRequest
import dev.mongocamp.server.model.{ MongoAggregateRequest, PipelineStage }
import org.bson.conversions.Bson

object AggregationService {

  def paginatedAggregation(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      pipeline: Seq[Bson],
      allowDiskUse: Boolean,
      rowsPerPage: Long,
      page: Long
  ): (List[Map[String, Any]], PaginationInfo) = {
    val mongoPaginatedFilter = MongoPaginatedAggregation(
      MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection),
      allowDiskUse,
      pipeline.toList
    )
    val aggregateResult = mongoPaginatedFilter.paginate(rowsPerPage, page)
    (aggregateResult.databaseObjects.map(MongoCampBsonConverter.documentToMap), aggregateResult.paginationInfo)
  }

  def paginatedAggregation(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      mongoAggregateRequest: MongoAggregateRequest,
      rowsPerPage: Long,
      page: Long
  ): (List[Map[String, Any]], PaginationInfo) = {
    val pipeline: Seq[Bson] = convertToBsonPipeline(mongoAggregateRequest.pipeline)
    paginatedAggregation(authorizedCollectionRequest, pipeline, mongoAggregateRequest.allowDiskUse, rowsPerPage, page)
  }

  def convertToBsonPipeline(pipeline: List[PipelineStage]): Seq[Bson] = {
    val response: Seq[Bson] = pipeline.map(
      element => {
        val stage = if (element.stage.startsWith("$")) element.stage else "$" + element.stage
        mapToBson(Map(stage -> element.value))
      }
    )
    response
  }
}
