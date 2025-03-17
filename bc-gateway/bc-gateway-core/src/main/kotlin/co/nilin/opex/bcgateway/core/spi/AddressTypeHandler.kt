package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.AddressType

interface AddressTypeHandler {

    suspend fun fetchAll(): List<AddressType>

    suspend fun addAddressType(name: String, addressRegex: String, memoRegex: String?)

    suspend fun fetchAddressType(name: String): AddressType?
}