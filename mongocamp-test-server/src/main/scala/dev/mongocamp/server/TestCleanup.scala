package dev.mongocamp.server

import dev.mongocamp.server.service.ReflectionService
import dev.mongocamp.server.test.MongoTestServer

class TestCleanup(name: String) {
  def cleanup(): Unit = {
    println("TestCleanup should to cleanup for " + name)
    val restServers = ReflectionService.instancesForType(classOf[RestServer])
    restServers.foreach(restServer => {
        restServer.shutdown()
    })
    MongoTestServer.stopMongoDatabase()
  }
}
