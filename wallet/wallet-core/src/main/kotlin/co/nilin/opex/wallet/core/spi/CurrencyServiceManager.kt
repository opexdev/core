package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.CurrenciesCommand
import co.nilin.opex.wallet.core.inout.CurrencyCommand
//import co.nilin.opex.wallet.core.model.Currencies
//import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.model.FetchCurrency

interface CurrencyServiceManager {

    suspend fun createNewCurrency(request: CurrencyCommand): CurrencyCommand?
    suspend fun fetchCurrencs(request: FetchCurrency): CurrenciesCommand?
    suspend fun fetchCurrency(request: FetchCurrency): CurrencyCommand?
    suspend fun updateCurrency(request: CurrencyCommand): CurrencyCommand?
    suspend fun prepareCurrencyToBeACryptoCurrency(request: String): CurrencyCommand?
    suspend fun deleteCurrencies(request: FetchCurrency)


}