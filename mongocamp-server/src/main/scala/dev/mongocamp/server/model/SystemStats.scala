package dev.mongocamp.server.model

/** The connections of the database server. */
case class ConnectionStats(current: Long, available: Long, totalCreated: Long)

/** The operations since the start of the database server (counters, a client computes rates from two reads). */
case class OpCounters(insert: Long, query: Long, update: Long, delete: Long, getmore: Long, command: Long)

/** The state of a replica set: the name of the set, the state of the member that answered (`PRIMARY`, `SECONDARY`, ...), the number of members and how many of
  * them are healthy. No host names or addresses of the members.
  */
case class ReplicaSetState(name: String, state: String, memberCount: Int, healthyMembers: Int)

/** The numbers of the database server for a server overview. `partial` is true when the database user of the server is not allowed to read the server status
  * (it needs the role clusterMonitor): `uptimeSeconds`, `connections` and `opcounters` are not given then. `replicaSet` is not given for a server that is not
  * part of a replica set.
  */
case class SystemStats(
  uptimeSeconds: Option[Long],
  connections: Option[ConnectionStats],
  opcounters: Option[OpCounters],
  replicaSet: Option[ReplicaSetState],
  partial: Boolean
)
