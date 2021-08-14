package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.AddressType
import co.nilin.opex.bcgateway.core.model.AssignedAddress
import co.nilin.opex.bcgateway.core.model.CachedAddress

interface CachedAddressHandler {
    suspend fun peekCachedAddress(addressType: AddressType): CachedAddress?
    suspend fun remove(cacheAddress: CachedAddress)
}