package dev.mongocamp.server.service

import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.exception.ErrorCodes
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.model.auth.AuthorizedCollectionRequest
import dev.mongocamp.server.model.PipelineStage

/** Refuses pipelines with operators that an operator of the server denied (`AGGREGATION_STAGES_DENIED`).
  *
  * The whole value tree is scanned and not only the names of the stages: `$function` and `$accumulator` are expressions inside `$project`, `$addFields`, `$group`
  * or `$match.$expr`, `$where` is inside `$match` and a pipeline can be nested in `$lookup`, `$unionWith` and `$facet`. It is used for the pipelines of the
  * clients, not for the internal pipelines (for example the schema analysis), which have no user and are not affected by the list.
  */
object AggregationPipelineGuard {

  def deniedOperators: Set[String] =
    ConfigurationService.getConfigValue[List[String]](DefaultConfigurations.ConfigKeyAggregationStagesDenied).map(normalize).toSet

  def check(pipeline: List[PipelineStage]): Unit = check(pipeline, deniedOperators)

  /** Checks the operators and the read grants of the user for every collection that a stage reads (`$lookup`, `$unionWith`, `$graphLookup`, nested ones too). */
  def check(authorizedCollectionRequest: AuthorizedCollectionRequest, pipeline: List[PipelineStage]): Unit = {
    check(pipeline)
    val user = authorizedCollectionRequest.userInformation
    // a collection of a stage is in the database of the collection of the request: `geodata:locations` + `accounts` is `geodata:accounts`
    val collection = authorizedCollectionRequest.collection
    val prefix     = if (collection.contains(":")) collection.substring(0, collection.indexOf(':') + 1) else ""
    pipeline
      .flatMap(
        stage => referencedCollections(Map(normalize(stage.stage) -> stage.value))
      )
      .foreach {
        case Right(name) =>
          if (!user.canRead(prefix + name)) {
            throw MongoCampException.forbiddenException(s"the user is not allowed to read the collection $name in an aggregation", ErrorCodes.forbiddenCollectionInStage)
          }
        case Left(description) =>
          throw MongoCampException.forbiddenException(s"$description is not supported in an aggregation", ErrorCodes.forbiddenCollectionInStage)
      }
  }

  /** The collections that stages in the value read: Right is a name, Left a form that is not supported (a lookup into another database). */
  def referencedCollections(value: Any): List[Either[String, String]] = {
    def fromValue(from: Any): List[Either[String, String]] = from match {
      case name: String            => List(Right(name))
      case _: collection.Map[_, _] => List(Left("a reference to a collection of another database"))
      case _                       => Nil
    }
    value match {
      case map: collection.Map[_, _] =>
        map.toList.flatMap(
          entry => {
            val own = (entry._1.toString, entry._2) match {
              case ("$lookup", m: collection.Map[_, _])      => fromValue(m.asInstanceOf[collection.Map[Any, Any]].getOrElse("from", null))
              case ("$graphLookup", m: collection.Map[_, _]) => fromValue(m.asInstanceOf[collection.Map[Any, Any]].getOrElse("from", null))
              case ("$unionWith", name: String)              => List(Right(name))
              case ("$unionWith", m: collection.Map[_, _])   => fromValue(m.asInstanceOf[collection.Map[Any, Any]].getOrElse("coll", null))
              case _                                         => Nil
            }
            own ++ referencedCollections(entry._2)
          }
        )
      case iterable: Iterable[_] => iterable.toList.flatMap(referencedCollections)
      case _                     => Nil
    }
  }

  def check(pipeline: List[PipelineStage], denied: Set[String]): Unit = {
    if (denied.nonEmpty) {
      pipeline.foreach(
        stage => {
          refuseIfDenied(normalize(stage.stage), denied)
          scan(stage.value, denied)
        }
      )
    }
  }

  // the stage name is accepted with and without `$`, like in the conversion to bson
  private def normalize(name: String): String = if (name.startsWith("$")) name else "$" + name

  private def refuseIfDenied(operator: String, denied: Set[String]): Unit = {
    if (denied.contains(operator)) {
      throw MongoCampException.forbiddenException(s"the operator $operator is not allowed in an aggregation", ErrorCodes.forbiddenStage)
    }
  }

  private def scan(value: Any, denied: Set[String]): Unit = {
    value match {
      case map: collection.Map[_, _] =>
        map.foreach(
          entry => {
            refuseIfDenied(entry._1.toString, denied)
            scan(entry._2, denied)
          }
        )
      case iterable: Iterable[_] => iterable.foreach(scan(_, denied))
      case array: Array[_]       => array.foreach(scan(_, denied))
      case javaMap: java.util.Map[_, _] =>
        javaMap.forEach(
          (key, v) => {
            refuseIfDenied(key.toString, denied)
            scan(v, denied)
          }
        )
      case javaCollection: java.util.Collection[_] => javaCollection.forEach(scan(_, denied))
      case _                                       =>
    }
  }
}
