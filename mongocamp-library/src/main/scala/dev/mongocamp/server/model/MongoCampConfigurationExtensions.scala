package dev.mongocamp.server.model

import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.model.MongoCampConfiguration._
import sttp.model.StatusCode

import scala.concurrent.duration.Duration

object MongoCampConfigurationExtensions {

  implicit class ExtendedMongoCampConfiguration(val mongoCampConfiguration: MongoCampConfiguration) extends AnyVal {
    def typedValue[A <: Any](): A = {

      val valueClass: Class[_] = {
        if (mongoCampConfiguration.configType.toLowerCase().contains(confTypeBoolean.toLowerCase())) {
          classOf[java.lang.Boolean]
        }
        else if (mongoCampConfiguration.configType.toLowerCase().contains(confTypeString.toLowerCase())) {
          classOf[String]
        }
        else if (mongoCampConfiguration.configType.toLowerCase().contains(confTypeDouble.toLowerCase())) {
          classOf[java.lang.Double]
        }
        else if (mongoCampConfiguration.configType.toLowerCase().contains(confTypeLong.toLowerCase())) {
          classOf[java.lang.Long]
        }
        else if (mongoCampConfiguration.configType.toLowerCase().contains(confTypeDuration.toLowerCase())) {
          Duration.getClass
        }
        else {
          {
            throw MongoCampException(s"value class ${mongoCampConfiguration.configType} is not supported at this moment", StatusCode.NotImplemented)
          }
        }
      }

      val resultClass: Class[_] =
        if (mongoCampConfiguration.configType.toLowerCase().startsWith("option")) {
          classOf[Option[valueClass.type]]
        }
        else if (mongoCampConfiguration.configType.toLowerCase().startsWith("list")) {
          classOf[::[valueClass.type]]
        }
        else {
          valueClass
        }

      val response = if (mongoCampConfiguration.value.isInstanceOf[Option[valueClass.type]]) {
        val valueOption = mongoCampConfiguration.value.asInstanceOf[Option[A]]
        if (resultClass == classOf[Option[valueClass.type]]) {
          valueOption
        }
        else if (valueOption.isDefined) {
          valueOption.get
        }
        else {
          if (mongoCampConfiguration.configType.equalsIgnoreCase(confTypeBoolean)) false
          else if (mongoCampConfiguration.configType.equalsIgnoreCase(confTypeString)) ""
          else if (mongoCampConfiguration.configType.equalsIgnoreCase(confTypeDouble)) 0.0
          else if (mongoCampConfiguration.configType.equalsIgnoreCase(confTypeLong)) 0L
          else if (mongoCampConfiguration.configType.equalsIgnoreCase(confTypeDuration)) Duration.Inf
          else if (mongoCampConfiguration.configType.toLowerCase().startsWith("list")) List()
          else if (mongoCampConfiguration.configType.toLowerCase().startsWith("option")) None
          else throw MongoCampException("no value set in database", StatusCode.PreconditionFailed)
        }
      }
      else {
        if (mongoCampConfiguration.configType.equalsIgnoreCase(confTypeDuration)) {
          Duration(mongoCampConfiguration.value.toString)
        }
        else if (
          mongoCampConfiguration.configType.equalsIgnoreCase(MongoCampConfiguration.confTypeDurationList) && mongoCampConfiguration.value
            .isInstanceOf[List[String]]
        ) {
          mongoCampConfiguration.value.asInstanceOf[List[String]].map(s => Duration(s))
        }
        else if (
          mongoCampConfiguration.configType.equalsIgnoreCase(MongoCampConfiguration.confTypeDurationOption) && mongoCampConfiguration.value
            .isInstanceOf[Option[String]]
        ) {
          mongoCampConfiguration.value.asInstanceOf[Option[String]].map(s => Duration(s))
        }
        else if (mongoCampConfiguration.configType.equalsIgnoreCase(confTypeLong) && mongoCampConfiguration.value.isInstanceOf[Int]) {
          mongoCampConfiguration.value.asInstanceOf[Int].toLong
        }
        else if (
          mongoCampConfiguration.value != null && mongoCampConfiguration.value.getClass.equals(resultClass) && mongoCampConfiguration.value.isInstanceOf[A]
        ) {
          mongoCampConfiguration.value
        }
        else if (
          mongoCampConfiguration.configType.equalsIgnoreCase(MongoCampConfiguration.confTypeLongList) && mongoCampConfiguration.value.isInstanceOf[List[Int]]
        ) {
          mongoCampConfiguration.value.asInstanceOf[List[Long]] // .map(s => s.toLong)
        }
        else if (
          mongoCampConfiguration.configType.equalsIgnoreCase(MongoCampConfiguration.confTypeLongOption) && mongoCampConfiguration.value
            .isInstanceOf[Option[Int]]
        ) {
          mongoCampConfiguration.value.asInstanceOf[Option[Long]] // .map(s => s.toLong)
        }
        else {
          if (mongoCampConfiguration.configType.toLowerCase.contains("List".toLowerCase))
            List()
          else if (mongoCampConfiguration.configType.toLowerCase.contains("Option".toLowerCase))
            None
          else {
            throw MongoCampException(s"no value set in database for key ${this.mongoCampConfiguration.key}", StatusCode.PreconditionFailed)
          }
        }
      }
      if (response.isInstanceOf[A]) {
        try
          response.asInstanceOf[A]
        catch {
          case e: Exception =>
            ""
            throw e
        }
      }
      else {
        throw MongoCampException(s"$response is not instance of requested type", StatusCode.PreconditionFailed)
      }
    }

    def validate: Boolean = {
      val internalConfigType = mongoCampConfiguration.configType.replace("List[", "").replace("]", "").replace("Option[", "").replace("]", "")
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

}
