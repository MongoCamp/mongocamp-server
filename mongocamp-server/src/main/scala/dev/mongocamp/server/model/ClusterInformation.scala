package dev.mongocamp.server.model

case class ClusterInformation(members: List[ClusterMemberInformation], coordinator: ClusterMemberInformation)
