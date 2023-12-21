package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.model.CurrencyImp
import co.nilin.opex.wallet.core.model.PropagateCurrencyChanges

interface BcGatewayProxy {
   suspend fun createCurrency(currencyImp: PropagateCurrencyChanges)

    suspend fun  updateCurrency()

    suspend fun getCurrencyInfo()
}