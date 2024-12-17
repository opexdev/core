package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.CryptoCurrencyCommand
import co.nilin.opex.bcgateway.core.model.FetchGateways
import co.nilin.opex.bcgateway.core.model.WithdrawData

interface CryptoCurrencyHandlerV2 {

    suspend fun createOnChainGateway(request: CryptoCurrencyCommand): CryptoCurrencyCommand?

    suspend fun updateOnChainGateway(request: CryptoCurrencyCommand): CryptoCurrencyCommand?

    suspend fun deleteOnChainGateway(gatewayUuid: String, currency: String): Void?

    suspend fun fetchCurrencyOnChainGateways(data: FetchGateways? = null): List<CryptoCurrencyCommand>?

    suspend fun fetchOnChainGateway(gatewayUuid: String, currency: String): CryptoCurrencyCommand?

    suspend fun changeWithdrawStatus(symbol: String, chain: String, status: Boolean)

    suspend fun getWithdrawData(symbol: String, network: String): WithdrawData

    suspend fun fetchGatewayWithoutSymbol(
        chain: String,
        isToken: Boolean,
        tokenAddress: String?
    ): CryptoCurrencyCommand?

}
