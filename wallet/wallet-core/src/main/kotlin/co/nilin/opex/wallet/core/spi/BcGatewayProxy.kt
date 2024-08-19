package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.OnChainGatewayCommand
import co.nilin.opex.wallet.core.inout.CurrencyGatewayCommand
import co.nilin.opex.wallet.core.inout.CurrencyGateways

interface BcGatewayProxy {
    suspend fun createGateway(currencyImp: CurrencyGatewayCommand, internalToken: String?): CurrencyGatewayCommand?

    suspend fun updateGateway(currencyImp: CurrencyGatewayCommand, internalToken: String?): CurrencyGatewayCommand?

    suspend fun fetchGateways(symbol: String?, internalToken: String?): CurrencyGateways?

    suspend fun fetchGatewayDetail(implUuid: String, currencySymbol: String, internalToken: String?): CurrencyGatewayCommand?

    suspend fun deleteGateway(implUuid: String, currencySymbol: String, internalToken: String?)

}