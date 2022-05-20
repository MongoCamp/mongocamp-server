package dev.mongocamp.server.interceptor.cors
import dev.mongocamp.server.config.ConfigHolder
import sttp.model.Header

object Cors {

  lazy val allowedOrigins: List[String] = ConfigHolder.corsOriginsAllowed.value.filter(_.nonEmpty)

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
      Header("Access-Control-Allow-Headers", ConfigHolder.corsHeadersAllowed.value.mkString(",")),
      Header("Access-Control-Expose-Headers", ConfigHolder.corsHeadersExposed.value.mkString(","))
    )
  }

}
