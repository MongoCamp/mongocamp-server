package dev.mongocamp.server.plugins.monitoring

import dev.mongocamp.server.test.MongoCampBaseServerSuite

abstract class MicrometerBaseServerSuite extends MongoCampBaseServerSuite{
  MicrometerServer.micrometerBasedServerStarted()
}
