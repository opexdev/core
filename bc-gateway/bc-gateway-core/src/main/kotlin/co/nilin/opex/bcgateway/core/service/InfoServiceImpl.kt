package co.nilin.opex.bcgateway.core.service

import co.nilin.opex.bcgateway.core.api.InfoService

class InfoServiceImpl : InfoService {
    override suspend fun countReservedAddresses(): Long {
        TODO()
    }

    override suspend fun getCurrencyInfo(): CurrencyInfo {
        TODO()
    }
}