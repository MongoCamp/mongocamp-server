package dev.mongocamp.server.converter

import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.mongodb.scala.bson.Document

import scala.collection.mutable

object MongoCampBsonConverter {
  def documentToMap(document: Document): Map[String, Any] = {
    val map = dev.mongocamp.driver.mongodb.bson.BsonConverter.asMap(document)
    map.map(
      element => {
        element._2 match {
          case objectId: ObjectId =>
            (element._1, objectId.toHexString)
          case _ =>
            (element._1, element._2)
        }
      }
    )
  }

  def documentToMap(document: org.bson.BsonDocument): Map[String, Any] = {
    val map = dev.mongocamp.driver.mongodb.bson.BsonConverter.asMap(document)
    map.map(
      element => {
        element._2 match {
          case objectId: ObjectId =>
            (element._1, objectId.toHexString)
          case _ =>
            (element._1, element._2)
        }
      }
    )
  }

  def convertToOperationMap(conversionMap: Map[String, Any]): Map[String, Any] = {

    val document = mutable.Map[String, Any]()

    convertFields(conversionMap).foreach(
      element => {
        if (element._1.startsWith("$")) {
          document.put(element._1, element._2)
        }
        else {
          if (element._2 == null) {
            addToOperationMap(document, "unset", (element._1, ""))
          }
          else {
            addToOperationMap(document, "set", element)
          }
        }

      }
    )
    document.toMap
  }

  private def addToOperationMap(document: mutable.Map[String, Any], operationType: String, element: (String, Any)): Option[Any] = {
    val setMap = document.getOrElse(
      "$" + operationType, {
        val map = mutable.Map[String, Any]()
        document.put("$" + operationType, map)
        map
      }
    )
    val map: mutable.Map[String, Any] = setMap match {
      case value: mutable.Map[String, Any] =>
        value
      case map: Map[String, Any] =>
        val mutableMap = mutable.Map[String, Any]()
        mutableMap ++ map
        mutableMap
    }
    map.put(element._1, element._2)
  }

  def convertFields(map: Map[String, Any]): Map[String, Any] = {
    val mutableMap = mutable.Map[String, Any]()
    map.foreach(
      element => {
        if (element._1 == "_id") {
          mutableMap.put(element._1, convertIdField(element._2))
        }
        else {
          element._2 match {
            case value: Map[String, Any] =>
              mutableMap.put(element._1, convertFields(value))
            case value: Iterable[Map[String, Any]] if (value.nonEmpty && value.head.isInstanceOf[Map[String, Any]]) =>
              mutableMap.put(
                element._1,
                value.map(
                  e => convertFields(e)
                )
              )
            case d: DateTime => mutableMap.put(element._1, d.toDate)
            case _ =>
              mutableMap.put(element._1, element._2)
          }
        }
      }
    )
    mutableMap.toMap
  }

  def convertIdField(id: Any): ObjectId = {
    id match {
      case s: String   => new ObjectId(s)
      case o: ObjectId => o
      case _           => new ObjectId(id.toString)
    }
  }
}
