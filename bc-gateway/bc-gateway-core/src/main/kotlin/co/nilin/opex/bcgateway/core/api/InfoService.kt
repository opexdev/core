package co.nilin.opex.bcgateway.core.api

import co.nilin.opex.bcgateway.core.model.CurrencyInfo

interface InfoService {
    suspend fun countReservedAddresses(): Long
    suspend fun getCurrencyInfo(): CurrencyInfo
}