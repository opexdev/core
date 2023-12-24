package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.model.CurrencyImp
import co.nilin.opex.wallet.core.model.PropagateCurrencyChanges
import co.nilin.opex.wallet.core.model.otc.Currency
import co.nilin.opex.wallet.core.model.otc.CurrencyImplementationResponse
import co.nilin.opex.wallet.core.model.otc.FetchCurrencyInfo

interface BcGatewayProxy {
   suspend fun createCurrency(currencyImp: PropagateCurrencyChanges):CurrencyImplementationResponse

    suspend fun  updateCurrency(currencyImp: PropagateCurrencyChanges):CurrencyImplementationResponse

    suspend fun getCurrencyInfo(symbol:String):FetchCurrencyInfo?

}