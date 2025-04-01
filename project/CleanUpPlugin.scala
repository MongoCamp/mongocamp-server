

object CleanUpPlugin {
  def cleanup(loader: java.lang.ClassLoader, name: String): Unit = {
    println("CleanUpPlugin should to cleanup for " + name)
    try {
      val testCleanUpClass = loader.loadClass("dev.mongocamp.server.TestCleanup")
      val testCleanupConstructors = testCleanUpClass.getConstructors.head
      val testCleanup = testCleanupConstructors.newInstance(name)
      testCleanUpClass.getMethod("cleanup").invoke(testCleanup)
    } catch {
      case e: java.lang.ClassNotFoundException =>
      case e: Exception =>
        println("Error while cleaning up: " + e)
    }
  }
}
