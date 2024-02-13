package dev.mongocamp.server.plugins.monitoring

import dev.mongocamp.server.test.TestServer.ignoreRunningInstanceAndReset

object MicrometerServer {
  private var micrometerStarted = false
  def micrometerBasedServerStarted(): Unit = {
    if (!micrometerStarted) {
      ignoreRunningInstanceAndReset()
      micrometerStarted = true
    }
  }
}
