package co.nilin.opex.port.bcgateway.postgres.impl

import co.nilin.opex.bcgateway.core.model.AddressType
import co.nilin.opex.bcgateway.core.model.CachedAddress
import co.nilin.opex.bcgateway.core.spi.CachedAddressHandler

class CachedAddressHandlerImpl: CachedAddressHandler {
    override suspend fun peekCachedAddress(addressType: AddressType): CachedAddress? {
        TODO("Not yet implemented")
    }

    override suspend fun remove(cacheAddress: CachedAddress) {
        TODO("Not yet implemented")
    }
}