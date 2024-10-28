package dev.mongocamp.server.converter

import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.model.ClusterMemberInformation

object JGroupsConverter {
  def convertAddress(address: org.jgroups.Address): ClusterMemberInformation = {
    ClusterMemberInformation(
      address.toString,
      address.isSiteMaster,
      address.isSiteAddress,
      address.isMulticast,
      coordinator = EventSystem.coordinator.equals(address)
    )
  }
}
