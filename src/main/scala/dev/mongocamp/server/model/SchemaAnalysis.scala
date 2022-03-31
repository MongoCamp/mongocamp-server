package dev.mongocamp.server.model
import dev.mongocamp.server.model

import scala.collection.mutable.ArrayBuffer

case class SchemaAnalysis(count: Long, sample: Long, percentageOfAnalysed: Double, fields: ArrayBuffer[model.SchemaAnalysisField])
