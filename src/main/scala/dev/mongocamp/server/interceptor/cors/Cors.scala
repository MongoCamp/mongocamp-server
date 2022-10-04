package dev.mongocamp.server.interceptor.cors
import dev.mongocamp.server.config.{ConfigManager, DefaultConfigurations}
import sttp.model.Header

object Cors {

  def allowedOrigins: List[String] = ConfigManager.getConfigValue[List[String]](DefaultConfigurations.ConfigKeyCorsOriginsAllowed).filter(_.nonEmpty)

  val KeyCorsHeaderOrigin  = "Origin"
  val KeyCorsHeaderReferer = "Referer"

  def corsHeadersFromOrigin(origin: Option[String]): List[Header] = {
    val allowedOrigin: String = if (origin.isDefined && allowedOrigins.contains(origin.get)) {
      origin.get
    }
    else if (allowedOrigins.isEmpty) {
      "*"
    }
    else {
      allowedOrigins.head
    }
    List(
      Header("Access-Control-Allow-Origin", allowedOrigin),
      Header("Access-Control-Allow-Credentials", "true"),
      Header("Access-Control-Allow-Headers", ConfigManager.getConfigValue[List[String]](DefaultConfigurations.ConfigKeyCorsHeadersAllowed).mkString(",")),
      Header("Access-Control-Expose-Headers", ConfigManager.getConfigValue[List[String]](DefaultConfigurations.ConfigKeyCorsHeadersExposed).mkString(","))
    )
  }

}
