package dev.mongocamp.server.model

case class MongoCampConfiguration(key: String, value: Any, configType: String, comment: String, needsRestartForActivation: Boolean)

object MongoCampConfiguration {
  val confTypeBoolean                = "Boolean"
  val confTypeBooleanList            = s"List[${confTypeBoolean}]"
  val confTypeBooleanOption          = s"Option[${confTypeBoolean}]"
  val confTypeString                 = "String"
  val confTypeStringList             = s"List[${confTypeString}]"
  val confTypeStringOption           = s"Option[${confTypeString}]"
  val confTypeDouble                 = "Double"
  val confTypeDoubleList             = s"List[${confTypeDouble}]"
  val confTypeDoubleOption           = s"Option[${confTypeDouble}]"
  val confTypeLong                   = "Long"
  val confTypeLongList               = s"List[${confTypeLong}]"
  val confTypeLongOption             = s"Option[${confTypeLong}]"
  val confTypeDuration               = "Duration"
  val confTypeDurationList           = s"List[${confTypeDuration}]"
  val confTypeDurationOption         = s"Option[${confTypeDuration}]"
  val allowedConfTypes: List[String] = List(confTypeBoolean, confTypeString, confTypeDuration, confTypeDouble, confTypeLong)
}
