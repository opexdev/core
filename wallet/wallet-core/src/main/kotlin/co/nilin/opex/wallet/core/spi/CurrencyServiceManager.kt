package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.CurrenciesCommand
import co.nilin.opex.wallet.core.inout.CurrencyCommand
//import co.nilin.opex.wallet.core.model.Currencies
//import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.model.FetchCurrency
import reactor.core.publisher.Mono

interface CurrencyServiceManager {

    suspend fun createNewCurrency(request: CurrencyCommand, ignoreIfExist:Boolean?=false ): CurrencyCommand?
    suspend fun fetchCurrencies(request: FetchCurrency): CurrenciesCommand?
    suspend fun fetchCurrency(request: FetchCurrency): CurrencyCommand?
    suspend fun updateCurrency(request: CurrencyCommand): CurrencyCommand?
    suspend fun prepareCurrencyToBeACryptoCurrency(request: String): CurrencyCommand?
    suspend fun deleteCurrency(request: FetchCurrency):Void?


}