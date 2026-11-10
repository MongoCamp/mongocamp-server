package dev.mongocamp.server.test.client.model

case class ConnectionStats(current: Long, available: Long, totalCreated: Long)

case class OpCounters(insert: Long, query: Long, update: Long, delete: Long, getmore: Long, command: Long)

case class ReplicaSetState(name: String, state: String, memberCount: Int, healthyMembers: Int)

case class SystemStats(
  uptimeSeconds: Option[Long] = None,
  connections: Option[ConnectionStats] = None,
  opcounters: Option[OpCounters] = None,
  replicaSet: Option[ReplicaSetState] = None,
  partial: Boolean
)
