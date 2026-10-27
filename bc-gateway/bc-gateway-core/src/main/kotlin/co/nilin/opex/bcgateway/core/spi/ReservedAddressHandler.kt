package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.AddressType
import co.nilin.opex.bcgateway.core.model.ReservedAddress
import co.nilin.opex.bcgateway.core.model.ReservedAddressFilter
import co.nilin.opex.bcgateway.core.model.ReservedAddressRecord

interface ReservedAddressHandler {
    suspend fun addReservedAddress(list: List<ReservedAddress>)

    /**
     * Takes one free address of this type out of the pool, atomically. Returns null when nothing
     * is left. This replaces reading an address and deleting it in two steps, which let two
     * concurrent assignments pick the same address.
     */
    suspend fun claimNext(addressType: AddressType): ReservedAddress?

    /**
     * Takes any free address out of the pool, atomically. The claimed address carries its own
     * type, so the caller does not have to pick one up front. Returns null when the pool is empty.
     */
    suspend fun claimAny(): ReservedAddress?

    /**
     * Takes this exact address out of the pool, atomically. Returns null when it is not in the
     * pool at all or another request claimed it first.
     */
    suspend fun claimByAddress(address: String, memo: String?): ReservedAddress?

    suspend fun findByAddress(address: String, memo: String?): ReservedAddress?

    suspend fun search(filter: ReservedAddressFilter): List<ReservedAddressRecord>

    suspend fun count(filter: ReservedAddressFilter): Long
}
