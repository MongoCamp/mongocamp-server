package dev.mongocamp.server.tests

import dev.mongocamp.server.test.MongoCampBaseServerSuite

class BaseServerSuite extends MongoCampBaseServerSuite {
  System.setProperty("PLUGINS_MODULES", "[\"dev.mongocamp:mongocamp-test-server_2.13:0.5.0\"]")
}
