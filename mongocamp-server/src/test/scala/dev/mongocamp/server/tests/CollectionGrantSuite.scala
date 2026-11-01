package dev.mongocamp.server.tests

import dev.mongocamp.server.auth.AuthHolder
import dev.mongocamp.server.database.TestAdditions
import dev.mongocamp.server.test.MongoCampBaseServerSuite

class CollectionGrantSuite extends MongoCampBaseServerSuite {

  private def user  = AuthHolder.handler.findUser(TestAdditions.testUser)
  private def admin = AuthHolder.handler.findUser(TestAdditions.adminUser)

  test("an admin may do everything on every collection") {
    assertEquals(admin.canRead("anything"), true)
    assertEquals(admin.canWrite("anything"), true)
    assertEquals(admin.canAdministrate("anything"), true)
  }

  test("a user is limited by the flags of the grant of the collection") {
    assertEquals((user.canRead("geodata:locations"), user.canWrite("geodata:locations"), user.canAdministrate("geodata:locations")), (true, false, false))
    assertEquals((user.canRead("accounts"), user.canWrite("accounts"), user.canAdministrate("accounts")), (true, true, false))
    assertEquals((user.canRead("test"), user.canWrite("test"), user.canAdministrate("test")), (true, true, true))
  }

  test("the name of the collection is compared ignoring the case") {
    assertEquals(user.canWrite("ACCOUNTS"), true)
  }

  test("a collection without a grant is not accessible") {
    assertEquals(user.canRead("other"), false)
    assertEquals(user.canWrite("other"), false)
    assertEquals(user.canAdministrate("other"), false)
  }
}
