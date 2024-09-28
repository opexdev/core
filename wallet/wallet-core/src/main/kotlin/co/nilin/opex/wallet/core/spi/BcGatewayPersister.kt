package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.CurrencyGatewayCommand
import co.nilin.opex.wallet.core.inout.WithdrawData

interface BcGatewayPersister {
    suspend fun createGateway(currencyImp: CurrencyGatewayCommand, internalToken: String?=null): CurrencyGatewayCommand?

    suspend fun updateGateway(currencyImp: CurrencyGatewayCommand, internalToken: String?=null): CurrencyGatewayCommand?

    suspend fun fetchGateways(symbol: String?, internalToken: String?=null): List<CurrencyGatewayCommand>?

    suspend fun fetchGatewayDetail(gatewayUuid: String, currencySymbol: String, internalToken: String?=null): CurrencyGatewayCommand?

    suspend fun deleteGateway(gatewayUuid: String, currencySymbol: String, internalToken: String?=null)

    suspend fun getWithdrawData(symbol: String, network: String): WithdrawData

}






