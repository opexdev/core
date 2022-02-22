package co.nilin.opex.bcgateway.core.spi

interface AddressTypeHandler {

    suspend fun addAddressType(name: String, addressRegex: String, memoRegex: String?)

}