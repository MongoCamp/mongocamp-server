package dev.mongocamp.server.converter

import dev.mongocamp.driver.mongodb.lucene.LuceneQueryConverter
import org.joda.time.IllegalFieldValueException
import sttp.tapir._

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

  def luceneQueryValidator(defaultField: String): Validator[String] = Validator.custom({ (x: String) =>
    val validation = LuceneQueryTools.validateLuceneQuery(x, defaultField)
    if (validation.valid) {
      ValidationResult.Valid
    }
    else {
      ValidationResult.Invalid(validation.validationError.get)
    }
  })

  case class LuceneQueryValidation(valid: Boolean, queryString: String, validationError: Option[String])

}
