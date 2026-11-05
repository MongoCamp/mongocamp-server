package dev.mongocamp.server.test.client.model

import org.joda.time.DateTime

case class IndexUsage(name: String, key: Map[String, Any], ops: Long, since: DateTime)
