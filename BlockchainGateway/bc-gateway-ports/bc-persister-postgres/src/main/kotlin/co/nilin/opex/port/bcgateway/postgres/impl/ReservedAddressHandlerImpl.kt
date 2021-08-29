package co.nilin.opex.port.bcgateway.postgres.impl

import co.nilin.opex.bcgateway.core.model.AddressType
import co.nilin.opex.bcgateway.core.model.ReservedAddress
import co.nilin.opex.bcgateway.core.spi.ReservedAddressHandler

class ReservedAddressHandlerImpl: ReservedAddressHandler {
    override suspend fun peekReservedAddress(addressType: AddressType): ReservedAddress? {
        TODO("Not yet implemented")
    }

    override suspend fun remove(reservedAddress: ReservedAddress) {
        TODO("Not yet implemented")
    }
}