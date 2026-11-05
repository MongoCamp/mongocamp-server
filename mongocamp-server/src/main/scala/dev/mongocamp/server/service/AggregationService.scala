package dev.mongocamp.server.service
import dev.mongocamp.driver.mongodb.mapToBson
import dev.mongocamp.server.converter.MongoCampBsonConverter
import dev.mongocamp.server.database.paging.MongoPaginatedAggregation
import dev.mongocamp.server.database.paging.PaginationInfo
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.exception.ErrorCodes
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.model.auth.AuthorizedCollectionRequest
import dev.mongocamp.server.model.MongoAggregateRequest
import dev.mongocamp.server.model.PipelineStage
import org.bson.conversions.Bson

object AggregationService {

  def paginatedAggregation(
    authorizedCollectionRequest: AuthorizedCollectionRequest,
    pipeline: Seq[Bson],
    allowDiskUse: Boolean,
    rowsPerPage: Long,
    page: Long
  ): (List[Map[String, Any]], PaginationInfo) = {
    val mongoPaginatedFilter =
      MongoPaginatedAggregation(MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection), allowDiskUse, pipeline.toList)
    val aggregateResult = mongoPaginatedFilter.paginate(rowsPerPage, page)
    (aggregateResult.databaseObjects.map(MongoCampBsonConverter.documentToMap), aggregateResult.paginationInfo)
  }

  def paginatedAggregation(
    authorizedCollectionRequest: AuthorizedCollectionRequest,
    mongoAggregateRequest: MongoAggregateRequest,
    rowsPerPage: Long,
    page: Long,
    matchFilter: Map[String, Any] = Map()
  ): (List[Map[String, Any]], PaginationInfo) = {
    AggregationPipelineGuard.check(authorizedCollectionRequest, mongoAggregateRequest.pipeline)
    val pipeline: Seq[Bson] = prependMatch(matchFilter, mongoAggregateRequest.pipeline) ++ convertToBsonPipeline(mongoAggregateRequest.pipeline)
    paginatedAggregation(authorizedCollectionRequest, pipeline, mongoAggregateRequest.allowDiskUse, rowsPerPage, page)
  }

  // stages that have to be the first stage of a pipeline, a $match before them is not possible
  private val firstStageOnly = Set("$geoNear", "$search", "$searchMeta", "$vectorSearch", "$collStats", "$indexStats", "$documents")

  /** The filter of the request as the first stage of the pipeline: the aggregation works on the documents that the filter matches (and uses an index for it). */
  private def prependMatch(matchFilter: Map[String, Any], pipeline: List[PipelineStage]): Seq[Bson] = {
    if (matchFilter.isEmpty) {
      Seq()
    }
    else {
      pipeline.headOption
        .map(
          stage => if (stage.stage.startsWith("$")) stage.stage else "$" + stage.stage
        )
        .filter(firstStageOnly.contains)
        .foreach(
          stage =>
            throw MongoCampException.badRequestException(
              s"the filter cannot be combined with a pipeline that starts with $stage, put the conditions in the stage itself",
              ErrorCodes.filterNotSupported
            )
        )
      Seq(mapToBson(Map("$match" -> MongoCampBsonConverter.convertFields(matchFilter))))
    }
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
