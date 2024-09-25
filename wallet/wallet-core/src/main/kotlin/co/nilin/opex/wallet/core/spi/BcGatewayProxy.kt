package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.WithdrawData
import co.nilin.opex.wallet.core.model.PropagateCurrencyChanges
import co.nilin.opex.wallet.core.model.otc.CurrencyImplementationResponse
import co.nilin.opex.wallet.core.model.otc.FetchCurrencyInfo
import java.math.BigDecimal

interface BcGatewayProxy {

    suspend fun createCurrency(currencyImp: PropagateCurrencyChanges): CurrencyImplementationResponse?

    suspend fun updateCurrency(currencyImp: PropagateCurrencyChanges): CurrencyImplementationResponse?

    suspend fun getCurrencyInfo(symbol: String): FetchCurrencyInfo?

    suspend fun getWithdrawData(symbol: String, network: String): WithdrawData

}