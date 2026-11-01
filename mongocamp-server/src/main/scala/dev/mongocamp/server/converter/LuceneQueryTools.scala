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

  /** Decodes the raw filter string. The result of the parse is not validated again: a tapir validator would run on the decoded query, which renders without the
    * quotes of the original (`city:"x:y"` becomes `city:x:y`) and does not parse again.
    */
  val filterCodec: Codec[String, Query, TextPlain] =
    Codec.string.mapDecode(
      (input: String) =>
        try {
          val query = LuceneQueryConverter.parse(input, "")
          // the conversion is part of the check, it is run again for the request
          LuceneQueryConverter.toDocument(query)
          DecodeResult.Value(query)
        }
        catch {
          case e: InvalidFilterException => DecodeResult.Error(input, e)
          case e: RuntimeException =>
            DecodeResult.Error(input, InvalidFilterException(Option(e.getMessage).filter(_.nonEmpty).getOrElse("the filter syntax is not supported")))
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
