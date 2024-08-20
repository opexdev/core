package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.CurrencyGatewayCommand
import co.nilin.opex.wallet.core.inout.CurrencyGateways

interface GatewayPersister {
    suspend fun createGateway(currencyImp: CurrencyGatewayCommand, internalToken: String?=null): CurrencyGatewayCommand?

    suspend fun updateGateway(currencyImp: CurrencyGatewayCommand, internalToken: String?=null): CurrencyGatewayCommand?

    suspend fun fetchGateways(symbol: String?, internalToken: String?=null): CurrencyGateways?

    suspend fun fetchGatewayDetail(implUuid: String, currencySymbol: String, internalToken: String?=null): CurrencyGatewayCommand?

    suspend fun deleteGateway(implUuid: String, currencySymbol: String, internalToken: String?=null)
}






