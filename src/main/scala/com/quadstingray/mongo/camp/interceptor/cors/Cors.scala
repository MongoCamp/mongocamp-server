package com.quadstingray.mongo.camp.interceptor.cors

import com.quadstingray.mongo.camp.config.Config
import sttp.model.Header

object Cors extends Config {

  lazy val allowedOrigins: List[String] = globalConfigStringList("cors.allowed.origins").filter(_.nonEmpty)

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
      Header("Access-Control-Allow-Headers", globalConfigStringList("cors.allowed.headers").mkString(","))
    )
  }

}
