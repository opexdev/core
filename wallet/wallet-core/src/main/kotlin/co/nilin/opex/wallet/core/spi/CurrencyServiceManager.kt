package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.CryptoCurrencyCommand
import co.nilin.opex.wallet.core.inout.CurrenciesCommand
import co.nilin.opex.wallet.core.inout.CurrencyCommand
//import co.nilin.opex.wallet.core.model.Currencies
//import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.model.FetchCurrency
import java.math.BigDecimal

interface CurrencyServiceManager {


    suspend fun createNewCurrency(request: CurrencyCommand): CurrencyCommand?
    suspend fun currency2Crypto(request: CryptoCurrencyCommand): CurrencyCommand?
    suspend fun fetchCurrencies(request: FetchCurrency): CurrenciesCommand?
    suspend fun updateCurrency(request: CurrencyCommand): CurrencyCommand?
    suspend fun prepareCurrencyToBeACryptoCurrency(request: String): CurrencyCommand?


    //    suspend fun getCurrency(symbol: String): Currency?
//    suspend fun addCurrency(name: String, symbol: String, precision: BigDecimal)
//    suspend fun addCurrency(request: Currency): Currency?
//    suspend fun updateCurrency(request: Currency): Currency?
    suspend fun editCurrency(name: String, symbol: String, precision: BigDecimal)
//    suspend fun deleteCurrency(name: String): Currencies
//
//    suspend fun getCurrencies(): Currencies
}