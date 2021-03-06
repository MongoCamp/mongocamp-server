package dev.mongocamp.server.service
import dev.mongocamp.server.converter.CirceSchema
import io.circe.parser.decode

import scala.io.Source

object SystemFileService extends CirceSchema {

  def readJsonList(fileName: String): List[Map[String, Any]] = {
    val fileContent = Source.fromResource(fileName).getLines.mkString
    val decoded     = decode[List[Map[String, Any]]](fileContent)
    decoded.getOrElse(List())
  }

  def readJson(fileName: String): Map[String, Any] = {
    val fileContent = Source.fromResource(fileName).getLines.mkString
    val decoded     = decode[Map[String, Any]](fileContent)
    decoded.getOrElse(Map())
  }

}
