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
    suspend fun fetchCurrencies(request: FetchCurrency): CurrenciesCommand?
    suspend fun updateCurrency(request: CurrencyCommand): CurrencyCommand?
    suspend fun prepareCurrencyToBeACryptoCurrency(request: String): CurrencyCommand?


}