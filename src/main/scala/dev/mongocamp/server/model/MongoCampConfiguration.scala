package dev.mongocamp.server.model

import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.model.MongoCampConfiguration._
import sttp.model.StatusCode

import scala.concurrent.duration.Duration

case class MongoCampConfiguration(key: String, value: Any, configType: String, comment: String, needsRestartForActivation: Boolean) {

  def typedValue[A <: Any](): A = {

    val valueClass: Class[_] = {
      if (configType.toLowerCase().contains(confTypeBoolean.toLowerCase())) classOf[java.lang.Boolean]
      else if (configType.toLowerCase().contains(confTypeString.toLowerCase())) classOf[String]
      else if (configType.toLowerCase().contains(confTypeDouble.toLowerCase())) classOf[java.lang.Double]
      else if (configType.toLowerCase().contains(confTypeLong.toLowerCase())) classOf[java.lang.Long]
      else if (configType.toLowerCase().contains(confTypeDuration.toLowerCase())) Duration.getClass
      else throw MongoCampException(s"value class $configType is not supported at this moment", StatusCode.NotImplemented)
    }

    val resultClass: Class[_] =
      if (configType.toLowerCase().startsWith("option")) classOf[Option[valueClass.type]]
      else if (configType.toLowerCase().startsWith("list")) classOf[::[valueClass.type]]
      else valueClass

    val response = if (value.isInstanceOf[Option[valueClass.type]]) {
      val valueOption = value.asInstanceOf[Option[A]]
      if(resultClass == classOf[Option[valueClass.type]]){
        valueOption
      }
      else if (valueOption.isDefined) {
        valueOption.get
      }
      else {
        if (configType.equalsIgnoreCase(confTypeBoolean)) false
        else if (configType.equalsIgnoreCase(confTypeString)) ""
        else if (configType.equalsIgnoreCase(confTypeDouble)) 0.0
        else if (configType.equalsIgnoreCase(confTypeLong)) 0L
        else if (configType.equalsIgnoreCase(confTypeDuration)) Duration.Inf
        else if (configType.toLowerCase().startsWith("list")) List()
        else if (configType.toLowerCase().startsWith("option")) None
        else throw MongoCampException("no value set in database", StatusCode.PreconditionFailed)
      }
    }
    else {
      if (configType.equalsIgnoreCase(confTypeDuration)) Duration(value.toString)
      else if (configType.equalsIgnoreCase(s"List[$confTypeDuration]") && value.isInstanceOf[List[String]]) {
        value.asInstanceOf[List[String]].map(s => Duration(s))
      }
      else if (configType.equalsIgnoreCase(s"Option[$confTypeDuration]") && value.isInstanceOf[Option[String]]) {
        value.asInstanceOf[Option[String]].map(s => Duration(s))
      }
      else if (configType.equalsIgnoreCase(confTypeLong) && value.isInstanceOf[Int]) {
        value.asInstanceOf[Int].toLong
      }
      else if (value.getClass.equals(resultClass) && value.isInstanceOf[A]) {
        value
      }
      else if (configType.equalsIgnoreCase(s"List[$confTypeLong]") && value.isInstanceOf[List[Int]]) {
        value.asInstanceOf[List[Int]].map(s => s.toLong)
      }
      else if (configType.equalsIgnoreCase(s"Option[$confTypeLong]") && value.isInstanceOf[Option[Int]]) {
        value.asInstanceOf[Option[Int]].map(s => s.toLong)
      }
      else {
        if (configType.toLowerCase.contains("List".toLowerCase)) List()
        else if (configType.toLowerCase.contains("Option".toLowerCase)) None
        else throw MongoCampException("no value set in database", StatusCode.PreconditionFailed)
      }
    }
    if (response.isInstanceOf[A]) {
      try {
        response.asInstanceOf[A]
      } catch {
        case e: Exception => {
          ""
          throw e
        }
      }
    } else {
      throw MongoCampException(s"$response is not instance of requested type", StatusCode.PreconditionFailed)
    }
  }

  def validate: Boolean = {
    val internalConfigType = configType.replace("List[", "").replace("]", "").replace("Option[", "").replace("]", "")
    val configTypeIsValid  = allowedConfTypes.map(_.toLowerCase).contains(internalConfigType.toLowerCase)
    val valueIsValid =
      try {
        val response = typedValue[Any]()
        response
        true
      }
      catch {
        case e: Exception =>
          false
      }
    configTypeIsValid && valueIsValid
  }

}

object MongoCampConfiguration {
  val confTypeBoolean                = "Boolean"
  val confTypeString                 = "String"
  val confTypeDouble                 = "Double"
  val confTypeLong                   = "Long"
  val confTypeDuration               = "Duration"
  val allowedConfTypes: List[String] = List(confTypeBoolean, confTypeString, confTypeDuration, confTypeDouble, confTypeLong)
}
