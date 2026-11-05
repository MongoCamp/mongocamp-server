package dev.mongocamp.server.tests

import dev.mongocamp.server.exception.ErrorCodes
import dev.mongocamp.server.exception.MongoCampException
import munit.FunSuite
import sttp.model.StatusCode

class MongoCampExceptionSuite extends FunSuite {

  test("bad request exception answers 400 with message and code") {
    val ex = MongoCampException.badRequestException("empty filter", ErrorCodes.emptyFilter)
    assertEquals(ex.statusCode, StatusCode.BadRequest)
    assertEquals(ex.getMessage, "empty filter")
    assertEquals(ex.errorCode, ErrorCodes.emptyFilter)
  }

  test("forbidden exception answers 403 with message and code") {
    val ex = MongoCampException.forbiddenException("stage not allowed", ErrorCodes.forbiddenStage)
    assertEquals(ex.statusCode, StatusCode.Forbidden)
    assertEquals(ex.errorCode, ErrorCodes.forbiddenStage)
  }

  test("error codes are unique") {
    val codes = List(
      ErrorCodes.emptyFilter,
      ErrorCodes.emptyBatch,
      ErrorCodes.ambiguousFilter,
      ErrorCodes.filterNotSupported,
      ErrorCodes.badInsertRequest,
      ErrorCodes.idMissingForReplace,
      ErrorCodes.forbiddenStage,
      ErrorCodes.forbiddenCollectionInStage,
      ErrorCodes.unauthorizedUser,
      ErrorCodes.unauthorizedUserForOtherUser
    )
    assertEquals(codes.distinct.size, codes.size)
  }
}
