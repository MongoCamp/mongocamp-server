package dev.mongocamp.server.service

import com.sfxcode.nosql.mongo._
import dev.mongocamp.server.converter.CirceSchema
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.model
import dev.mongocamp.server.model.auth.AuthorizedCollectionRequest
import dev.mongocamp.server.model.{FieldType, PipelineStage, SchemaAnalysis}
import dev.mongocamp.server.service.AggregationService.convertToBsonPipeline
import org.bson.conversions.Bson

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.DurationInt

object SchemaService extends CirceSchema {
  private val NameSeparator: String = "."
  private val FieldSplitter: String = "_/_"
  private val ArrayItemMark: String = "[]"
  private val KeyFieldType          = "$$t"
  private val ObjectName            = "xl"
  private val ArrayName             = "xa"

  private def schemaAggregation(deepth: Int, sampleSize: Option[Int]): List[PipelineStage] = {
    val buffer = ArrayBuffer[PipelineStage]()
    buffer.addAll(sampleSize.map(size => PipelineStage("sample", Map("size" -> size))))

    buffer.addOne(PipelineStage("project", Map("_" -> processObject(deepth, 0, "$$ROOT", List()), "_id" -> 0)))

    (0 to deepth).foreach(_ => {
      buffer.addOne(PipelineStage("unwind", Map("path" -> "$_", "preserveNullAndEmptyArrays" -> true)))
      buffer.addOne(PipelineStage("replaceRoot", Map("newRoot" -> Map("$cond" -> List(Map("$eq" -> List("$_", null)), "$$ROOT", "$_")))))
    })

    buffer.addAll(
      List(
        PipelineStage("project", Map("_" -> 0)),
        PipelineStage("project", Map("l" -> "$$REMOVE", "n" -> 1, "t" -> 1, "v" -> "$$REMOVE")),
        PipelineStage("group", Map("_id" -> Map("n" -> "$n", "t" -> "$t"), "c" -> Map("$sum" -> 1))),
        PipelineStage("facet", Map("bS" -> List(Map("$project" -> Map("_id" -> Map("n" -> "$_id.n", "sT" -> "bS", "t" -> "$_id.t"), "c" -> 1))))),
        PipelineStage("project", Map("data" -> Map("$concatArrays" -> List("$bS")))),
        PipelineStage("unwind", "$data"),
        PipelineStage("replaceRoot", Map("newRoot" -> "$data")),
        PipelineStage("group", SystemFileService.readJson("schema_stage11_group.json")),
        PipelineStage("replaceRoot", Map("newRoot" -> Map("$mergeObjects" -> "$S"))),
        PipelineStage("sort", Map("t" -> 1)),
        PipelineStage("group", Map("T" -> Map("$push" -> "$$ROOT"), "_id" -> Map("n" -> "$n"), "c" -> Map("$sum" -> "$c"))),
        PipelineStage("project", Map("T" -> 1, "_id" -> 0, "c" -> 1, "n" -> "$_id.n")),
        PipelineStage("sort", Map("n" -> 1))
      )
    )
    buffer.toList
  }

  case class AggregationField(name: String, value: String, level: Int)

  def createBranch(`case`: Bson, `then`: Bson): Bson = Map("case" -> `case`, "then" -> `then`)
  def createLet(in: Bson, vars: Bson): Bson          = Map("$let" -> Map("in" -> in, "vars" -> vars))

  def fieldValue(fieldName: String, fieldLevel: Int) = {
    Map("_" -> null, "e" -> fieldLevel, "n" -> generateFieldName(fieldName), "t" -> KeyFieldType)
  }

  def generateFieldName(fieldName: String): Any = {
    val field = fieldName
      .replace("$$", "")
      .replace(ObjectName, FieldSplitter ++ ObjectName)
      .replace(ArrayName, FieldSplitter + ArrayName)
      .replace(ArrayItemMark, FieldSplitter + ArrayItemMark)
    val fields                             = field.split(FieldSplitter).filterNot(s => s == null || s.isEmpty || s.isBlank)
    val responseArray: ArrayBuffer[String] = ArrayBuffer()
    fields.toList
      .map(string => string.replace(ObjectName, "$$" ++ ObjectName))
      .foreach(string => {
        var fieldName = string
        if (fieldName.startsWith(NameSeparator)) {
          responseArray.addOne(NameSeparator)
          fieldName = fieldName.substring(1)
        }
        val hasEndingSeperator: Boolean = if (fieldName.endsWith(NameSeparator)) {
          fieldName = fieldName.substring(0, fieldName.length - 1)
          true
        }
        else {
          false
        }
        responseArray.addOne(fieldName)
        if (hasEndingSeperator) {
          responseArray.addOne(NameSeparator)
        }
      })

    if (responseArray.size == 1) {
      var result = responseArray.head
      if (!result.startsWith("$$")) {
        result = "$$%s".format(result)
      }
      result
    }
    else {
      Map("$concat" -> responseArray.toList)
    }
  }

  def processField(maxLevel: Int, field: AggregationField, parents: List[String]): Bson = {
    val newParents       = addToParents(parents, field.name)
    val fullName: String = if (parents.isEmpty) field.name else newParents.mkString
    val stringBranch     = createBranch(Map("$eq" -> List(KeyFieldType, "string")), fieldValue(fullName, field.level))
    val arrayBranch      = createBranch(Map("$eq" -> List(KeyFieldType, "array")), processArrayField(maxLevel, fullName, field, parents))
    val objectBranch     = createBranch(Map("$eq" -> List(KeyFieldType, "object")), processObjectField(maxLevel, fullName, field, parents))
    Map(
      "$switch" -> Map(
        "branches" -> List(stringBranch, arrayBranch, objectBranch),
        "default"  -> fieldValue(fullName, field.level)
      )
    )
  }

  def processArrayField(maxLevel: Int, fullName: String, field: AggregationField, parents: List[String]): Bson = {
    val nestedObject = if (field.level >= maxLevel) {
      null
    }
    else {
      Map("$concatArrays" -> List(List(null), processArray(maxLevel, field, parents)))
    }
    Map("_" -> nestedObject, "n" -> generateFieldName(fullName), "t" -> KeyFieldType, "e" -> field.level)
  }
  def processArray(maxLevel: Int, field: AggregationField, parents: List[String]): Bson = {
    val level   = field.level
    val itemVar = s"$ArrayName$level"
    val item    = AggregationField(ArrayItemMark, itemVar, level + 1)
    Map(
      "$map" -> Map(
        "as"    -> itemVar,
        "in"    -> createLet(processField(maxLevel, item, addToParents(parents, field.name)), createTypeField(item)),
        "input" -> generateFieldName(field.value)
      )
    )
  }

  def addToParents(list: List[String], newElement: String): List[String] = {
    if (!list.contains(newElement) || newElement.equalsIgnoreCase(ArrayItemMark)) {
      if (list.isEmpty) {
        List(newElement)
      }
      else {
        list ++ List(NameSeparator, newElement)
      }
    }
    else {
      list
    }
  }

  def processObjectField(maxLevel: Int, fullName: String, field: AggregationField, parents: List[String]): Bson = {
    val nestedObject = if (field.level >= maxLevel) {
      null
    }
    else {
      Map("$concatArrays" -> List(List(null), processObject(maxLevel, field.level + 1, field.value, addToParents(parents, field.name))))
    }
    Map(
      "_" -> nestedObject,
      "n" -> generateFieldName(fullName),
      "t" -> KeyFieldType,
      "e" -> field.level
    )
  }

  def processObject(maxLevel: Int, level: Int, objectName: String, parents: List[String]): Bson = {
    val itemVar            = s"$ObjectName$level"
    val field              = AggregationField(s"$itemVar.k", s"$itemVar.v", level)
    val objectNameFunction = if (objectName.startsWith("$$")) objectName else "$$" + objectName
    Map(
      "$map" -> Map(
        "as"    -> itemVar,
        "in"    -> createLet(processField(maxLevel, field, parents), createTypeField(field)),
        "input" -> Map("$objectToArray" -> objectNameFunction)
      )
    )
  }

  def createTypeField(field: AggregationField): Map[String, Any] = {
    val fieldValue = if (field.value.startsWith("$$")) field.value else "$$" + field.value
    Map("t" -> Map("$type" -> fieldValue))
  }

  val emptyField = model.SchemaAnalysisField("ROOT","", List(), -1, -1, ArrayBuffer())

  def analyzeSchema(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      deepth: Int,
      sample: Option[Int]
  ): SchemaAnalysis = {
    val dao           = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
    val dbResponse    = dao.findAggregated(convertToBsonPipeline(schemaAggregation(deepth, sample)), allowDiskUse = true).resultList(3.minutes.toSeconds.toInt)
    val countResponse = dao.count().result()
    val sampledDataCountOption: Option[Long] = dbResponse.find(document => document.getString("n").equalsIgnoreCase("_id")).map(_.getLongValue("c"))
    val sampledDataCount                     = sampledDataCountOption.getOrElse(-1L)
    val fieldsMap                            = mutable.Map[String, model.SchemaAnalysisField]()
    fieldsMap.put(emptyField.name, emptyField.copy(count = sampledDataCount))
    dbResponse.foreach(document => {
      val documentMap        = mapFromDocument(document)
      val fullName           = documentMap.get("n").map(_.toString).getOrElse("")
      var name: String       = fullName
      var parentName: String = emptyField.name
      var percentage: Double = 0
      val fieldCount         = document.getLongValue("c")

      if (fullName.contains(NameSeparator)) {
        val fieldNames = fullName.split(NameSeparator.charAt(0))
        name = fieldNames.last
        val parentFields = fieldNames.splitAt(fieldNames.length - 1)
        parentName = parentFields._1.mkString(".")
      }
      else {
        percentage = fieldCount / sampledDataCount.toDouble
      }

      val parent = fieldsMap.getOrElse(
        parentName, {
          val newF = emptyField.copy(name = parentName)
          fieldsMap.put(parentName, newF)
          newF
        }
      )

      if (fullName.contains(NameSeparator)) {
        percentage = fieldCount.toDouble / parent.count.toDouble
      }
      val types: List[FieldType] = documentMap
        .get("T")
        .map(_.asInstanceOf[List[org.mongodb.scala.bson.collection.immutable.Document]])
        .getOrElse(List())
        .map(typeDocument => {
          val doc                         = documentFromDocument(typeDocument)
          val count                       = doc.getLongValue("c")
          val fieldTypePercentage: Double = count.toDouble / parent.count.toDouble
          FieldType(doc.getStringValue("t"), count, fieldTypePercentage)
        })

      val newField = model.SchemaAnalysisField(name.replace(ArrayItemMark, "[array element]"), fullName, types, fieldCount, percentage, ArrayBuffer())

      parent.subFields.addOne(newField)
      fieldsMap.put(s"$parentName$NameSeparator$name".replace("ROOT.", ""), newField)
    })

    val fieldPercentage: Double = sampledDataCount / countResponse
    model.SchemaAnalysis(countResponse, sampledDataCount, fieldPercentage, fieldsMap.get("ROOT").map(_.subFields).getOrElse(ArrayBuffer()))
  }

  def detectSchema: List[]
}
