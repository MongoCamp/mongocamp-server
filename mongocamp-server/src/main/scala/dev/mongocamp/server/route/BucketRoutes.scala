package dev.mongocamp.server.route

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.event.bucket.{ ClearBucketEvent, DropBucketEvent }
import dev.mongocamp.server.exception.ErrorDescription
import dev.mongocamp.server.file.FileAdapterHolder
import dev.mongocamp.server.model.BucketInformation.BucketCollectionSuffix
import dev.mongocamp.server.model.auth.{ AuthorizedCollectionRequest, UserInformation }
import dev.mongocamp.server.model.{ BucketInformation, JsonValue, ModelConstants }
import dev.mongocamp.server.plugin.RoutesPlugin
import io.circe.generic.auto._
import sttp.capabilities
import sttp.capabilities.pekko.PekkoStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

object BucketRoutes extends BucketBaseRoute with RoutesPlugin {

  private val apiName = "Bucket"

  val bucketListEndpoint = securedEndpoint
    .in(mongoDbPath)
    .in("buckets")
    .out(jsonBody[List[String]])
    .summary("List of Buckets")
    .description("List of all Buckets of the default database")
    .tag(apiName)
    .method(Method.GET)
    .name("listBuckets")
    .serverLogic(
      user => _ => bucketsList(user)
    )

  def bucketsList(userInformation: UserInformation): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[String]]] = {
    Future.successful(Right({
      val result = MongoDatabase.databaseProvider.collectionNames().filter(_.endsWith(BucketCollectionSuffix)).map(_.replace(BucketCollectionSuffix, ""))
      val bucketGrants =
        userInformation.getGrants.filter(
          g => g.grantType.equals(ModelConstants.grantTypeBucket) || g.grantType.equals(ModelConstants.grantTypeBucketMeta)
        )
      result.filter(
        collection => {
          val readBuckets = bucketGrants.filter(_.read).map(_.name)
          userInformation.isAdmin || readBuckets.contains(AuthorizedCollectionRequest.all) || readBuckets.contains(collection)
        }
      )
    }))
  }

  val bucketEndpoint = readBucketEndpoint
    .out(jsonBody[BucketInformation])
    .summary("Bucket Information")
    .description("All Information about a single Bucket")
    .tag(apiName)
    .method(Method.GET)
    .name("getBucket")
    .serverLogic(
      authorizedCollectionRequest => _ => getBucket(authorizedCollectionRequest)
    )

  def getBucket(
      authorizedCollectionRequest: AuthorizedCollectionRequest
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), BucketInformation]] = {
    Future.successful(Right({
      val collectionStatus = MongoDatabase.databaseProvider.dao(s"${authorizedCollectionRequest.collection}$BucketCollectionSuffix").collectionStatus.result()
      val fileSizes        = FileAdapterHolder.handler.size(authorizedCollectionRequest.collection)
      val completeSize     = fileSizes + collectionStatus.size
      BucketInformation(authorizedCollectionRequest.collection, collectionStatus.count, completeSize, completeSize / collectionStatus.count)
    }))
  }

  val deleteBucketEndpoint = writeBucketEndpoint
    .out(jsonBody[JsonValue[Boolean]])
    .summary("Delete Bucket")
    .description("Delete a given Bucket")
    .tag(apiName)
    .method(Method.DELETE)
    .name("deleteBucket")
    .serverLogic(
      authorizedCollectionRequest => _ => deleteBucket(authorizedCollectionRequest)
    )

  def deleteBucket(
      authorizedCollectionRequest: AuthorizedCollectionRequest
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonValue[Boolean]]] = {
    Future.successful(Right({
      MongoDatabase.databaseProvider.dao(s"${authorizedCollectionRequest.collection}$BucketCollectionSuffix").drop().result()
      FileAdapterHolder.handler.delete(authorizedCollectionRequest.collection)
      EventSystem.publish(DropBucketEvent(authorizedCollectionRequest.userInformation, authorizedCollectionRequest.collection))
      JsonValue[Boolean](true)
    }))
  }

  val clearBucketEndpoint = writeBucketEndpoint
    .in("clear")
    .out(jsonBody[JsonValue[Boolean]])
    .summary("Clear Bucket")
    .description("Delete all Files in Bucket")
    .tag(apiName)
    .method(Method.DELETE)
    .name("clearBucket")
    .serverLogic(
      authorizedCollectionRequest => _ => clearBucket(authorizedCollectionRequest)
    )

  def clearBucket(
      authorizedCollectionRequest: AuthorizedCollectionRequest
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonValue[Boolean]]] = {
    Future.successful(Right({
      MongoDatabase.databaseProvider.dao(s"${authorizedCollectionRequest.collection}$BucketCollectionSuffix").deleteAll().result()
      val clearResponse = FileAdapterHolder.handler.clear(authorizedCollectionRequest.collection)
      EventSystem.publish(ClearBucketEvent(authorizedCollectionRequest.userInformation, authorizedCollectionRequest.collection))
      JsonValue[Boolean](clearResponse)
    }))
  }

  override def endpoints: List[ServerEndpoint[PekkoStreams with capabilities.WebSockets, Future]] =
    List(
      bucketListEndpoint,
      bucketEndpoint,
      deleteBucketEndpoint,
      clearBucketEndpoint
    )

}
