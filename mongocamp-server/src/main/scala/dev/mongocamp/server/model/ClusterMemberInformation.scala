package dev.mongocamp.server.model

case class ClusterMemberInformation(name: String, siteMaster: Boolean, siteAddress: Boolean, multicast: Boolean, coordinator: Boolean)
