package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.AddressType
import co.nilin.opex.bcgateway.core.model.ReservedAddress
import co.nilin.opex.bcgateway.core.model.ReservedAddressV2

interface ReservedAddressHandler {
    suspend fun addReservedAddress(list: List<ReservedAddress>)
    suspend fun peekReservedAddress(addressType: AddressType): ReservedAddress?

    suspend fun peekReservedAddress(addressTypeId: Long): ReservedAddressV2?

    suspend fun remove(reservedAddress: ReservedAddress)

    suspend fun remove(reservedAddress: ReservedAddressV2)

}