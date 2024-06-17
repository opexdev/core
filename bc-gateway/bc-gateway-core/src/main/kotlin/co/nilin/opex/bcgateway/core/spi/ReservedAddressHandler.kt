package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.AddressType
import co.nilin.opex.bcgateway.core.model.ReservedAddress

interface ReservedAddressHandler {
    suspend fun addReservedAddress(list: List<ReservedAddress>)
    suspend fun peekReservedAddress(addressType: AddressType): ReservedAddress?
    suspend fun remove(reservedAddress: ReservedAddress)
}