package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.AddressStatus
import co.nilin.opex.bcgateway.core.model.AddressType
import co.nilin.opex.bcgateway.core.model.AssignedAddress
import java.time.LocalDateTime

interface AssignedAddressHandler {
    suspend fun fetchAssignedAddresses(user: String, addressTypes: List<AddressType>): List<AssignedAddress>
    suspend fun persist(assignedAddress: AssignedAddress)

    suspend fun revoke(assignedAddress: AssignedAddress)

    suspend fun findUuid(address: String, memo: String?): String?

    suspend fun fetchExpiredAssignedAddresses(): List<AssignedAddress>?

    /**
     * Find the holder (uuid) and status of an address at a given time.
     * If [at] is null, returns the current holder (status Assigned) if any.
     * Returns Pair<uuid, status>, where both can be null when unassigned.
     */
    suspend fun findHolder(address: String, memo: String?, at: LocalDateTime?): Pair<String?, AddressStatus?>
}