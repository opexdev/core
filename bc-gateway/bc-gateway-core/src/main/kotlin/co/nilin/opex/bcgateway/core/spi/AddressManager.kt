package co.nilin.opex.bcgateway.core.spi

interface AddressManager {

    suspend fun revokeExpiredAddress()
}