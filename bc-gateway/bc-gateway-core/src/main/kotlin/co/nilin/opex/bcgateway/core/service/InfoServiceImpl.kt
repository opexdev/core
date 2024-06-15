package co.nilin.opex.bcgateway.core.service

import co.nilin.opex.bcgateway.core.api.InfoService
import co.nilin.opex.bcgateway.core.model.CryptoCurrencyCommand

class InfoServiceImpl : InfoService {
    override suspend fun countReservedAddresses(): Long {
        TODO()
    }

    override suspend fun getCurrencyInfo(): CryptoCurrencyCommand {
        TODO()
    }
}