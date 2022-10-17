package dev.mongocamp.server.model

case class MongoCampConfiguration(key: String, value: Any, configType: String, comment: String, needsRestartForActivation: Boolean) {
}

object MongoCampConfiguration {
  val confTypeBoolean                = "Boolean"
  val confTypeString                 = "String"
  val confTypeDouble                 = "Double"
  val confTypeLong                   = "Long"
  val confTypeDuration               = "Duration"
  val allowedConfTypes: List[String] = List(confTypeBoolean, confTypeString, confTypeDuration, confTypeDouble, confTypeLong)
}
