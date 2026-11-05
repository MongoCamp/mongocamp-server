package dev.mongocamp.server.test.client.model

case class SearchRequest(filter: Option[String] = None, sort: Seq[String] = Seq(), projection: Seq[String] = Seq())
