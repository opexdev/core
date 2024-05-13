package co.nilin.opex.bcgateway.core.api

interface InfoService {
    suspend fun countReservedAddresses(): Long
    suspend fun getCurrencyInfo(): CurrencyInfo
}