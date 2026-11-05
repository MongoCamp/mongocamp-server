package dev.mongocamp.server.converter

import dev.mongocamp.driver.mongodb.lucene.LuceneQueryConverter
import org.apache.lucene.search.Query
import org.joda.time.IllegalFieldValueException
import sttp.tapir._
import sttp.tapir.CodecFormat.TextPlain

object LuceneQueryTools {
  def validateLuceneQuery(luceneQueryInput: String, defaultField: String): LuceneQueryValidation = {
    try {
      LuceneQueryConverter.parse(luceneQueryInput, defaultField)
      LuceneQueryValidation(valid = true, luceneQueryInput, None)
    }
    catch {
      case e: IllegalFieldValueException =>
        validationResultFromException(luceneQueryInput, e)
      case e: IllegalArgumentException =>
        validationResultFromException(luceneQueryInput, e)
    }
  }

  private def validationResultFromException(luceneQueryInput: String, e: Exception): LuceneQueryValidation = {
    LuceneQueryValidation(valid = false, luceneQueryInput, Some(e.getMessage))
  }

  /** A filter that cannot be parsed or that the driver cannot convert (for example open ranges, which fail with a NullPointerException there). */
  case class InvalidFilterException(reason: String) extends RuntimeException(reason)

  /** Parses a filter string. The conversion is part of the check, it is run again for the request. Every failure is an [[InvalidFilterException]]. */
  def parseFilter(input: String): Query =
    try {
      val query = LuceneQueryConverter.parse(input, "")
      LuceneQueryConverter.toDocument(query)
      query
    }
    catch {
      case e: InvalidFilterException => throw e
      case e: Exception =>
        // a header cannot carry a multi line message, and the parser messages have several lines
        val reason = Option(e.getMessage).flatMap(_.linesIterator.map(_.trim).find(_.nonEmpty)).getOrElse("the filter syntax is not supported")
        throw InvalidFilterException(reason)
    }

  /** The filter of a parsed query as a map for the database, an empty map without a query. The `_id` and date conversion of the document routes is applied
    * afterwards by `MongoCampBsonConverter.convertFields`, as for every other filter.
    */
  def toFilterMap(query: Option[Query]): Map[String, Any] =
    query.map(q => MongoCampBsonConverter.documentToMap(LuceneQueryConverter.toDocument(q).toBsonDocument)).getOrElse(Map())

  /** Decodes the raw filter string. The result of the parse is not validated again: a tapir validator would run on the decoded query, which renders without the
    * quotes of the original (`city:"x:y"` becomes `city:x:y`) and does not parse again.
    */
  val filterCodec: Codec[String, Query, TextPlain] =
    Codec.string.mapDecode(
      (input: String) =>
        try DecodeResult.Value(parseFilter(input))
        catch {
          case e: InvalidFilterException => DecodeResult.Error(input, e)
        }
    )(_.toString)

  def luceneQueryValidator(defaultField: String): Validator[String] = Validator.custom {
    (x: String) =>
      val validation = LuceneQueryTools.validateLuceneQuery(x, defaultField)
      if (validation.valid) {
        ValidationResult.Valid
      }
      else {
        ValidationResult.Invalid(validation.validationError.get)
      }
  }

  case class LuceneQueryValidation(valid: Boolean, queryString: String, validationError: Option[String])

}
