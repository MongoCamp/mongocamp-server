package dev.mongocamp.server.model
import io.circe.generic.auto._
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder }
import sttp.tapir.Schema
import sttp.tapir.generic.auto._

import scala.collection.mutable.ArrayBuffer

case class SchemaAnalysisField(
    name: String,
    fullName: String,
    fieldTypes: List[SchemaAnalysisFieldType],
    count: Long,
    percentageOfParent: Double,
    subFields: ArrayBuffer[SchemaAnalysisField]
)

// recursive type: circe auto derivation can not resolve it on Scala 3, so the codecs are derived explicitly (and lazily)
object SchemaAnalysisField {
  implicit lazy val schemaAnalysisFieldEncoder: Encoder[SchemaAnalysisField] = deriveEncoder[SchemaAnalysisField]
  implicit lazy val schemaAnalysisFieldDecoder: Decoder[SchemaAnalysisField] = deriveDecoder[SchemaAnalysisField]
  // must be a def, not a lazy val: the derivation needs this schema again for subFields while it is still running and a
  // Scala 3 lazy val would block on its own initialisation (tapir's derivation cache terminates the recursion instead)
  implicit def schemaAnalysisFieldSchema: Schema[SchemaAnalysisField] = Schema.derived[SchemaAnalysisField]
}
