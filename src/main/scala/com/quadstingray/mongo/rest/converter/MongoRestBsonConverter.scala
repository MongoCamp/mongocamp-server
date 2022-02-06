package com.quadstingray.mongo.rest.converter
import org.mongodb.scala.bson.{ Document, ObjectId }

object MongoRestBsonConverter {

  def documentToMap(document: Document): Map[String, Any] = {
    val map = com.sfxcode.nosql.mongo.bson.BsonConverter.asMap(document)
    map.map(element => {
      element._2 match {
        case objectId: ObjectId =>
          (element._1, objectId.toHexString)
        case _ =>
          (element._1, element._2)
      }
    })
  }

}
