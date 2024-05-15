package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.AddressType
import co.nilin.opex.bcgateway.core.model.AssignedAddress
import co.nilin.opex.bcgateway.core.model.AssignedAddressV2
import java.time.LocalDateTime

interface AssignedAddressHandler {
    suspend fun fetchAssignedAddresses(user: String, addressTypes: List<AddressType>): List<AssignedAddress>

    suspend fun fetchAssignedAddresses(user: String, addressType:Long): AssignedAddressV2?

    suspend fun persist(assignedAddress: AssignedAddress)
    suspend fun persist(user:String,assignedAddress: AssignedAddressV2):AssignedAddressV2?


    suspend fun revoke(assignedAddress: AssignedAddress)

    suspend fun findUuid(address: String, memo: String?): String?

    suspend fun fetchExpiredAssignedAddresses(): List<AssignedAddress>?

}