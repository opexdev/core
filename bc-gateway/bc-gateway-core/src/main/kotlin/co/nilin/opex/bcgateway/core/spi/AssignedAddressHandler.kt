package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.AddressType
import co.nilin.opex.bcgateway.core.model.AssignedAddress

interface AssignedAddressHandler {
    suspend fun fetchAssignedAddresses(user: String, addressTypes: List<AddressType>): List<AssignedAddress>
    suspend fun persist(assignedAddress: AssignedAddress)
    suspend fun findUuid(address: String, memo: String?): String?
}