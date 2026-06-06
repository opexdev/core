package co.nilin.opex.wallet.core.spi


import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.model.FetchCurrency
import java.math.BigDecimal

interface CurrencyServiceManager {

    suspend fun createNewCurrency(request: CurrencyCommand, ignoreIfExist: Boolean? = false): CurrencyCommand?
    suspend fun fetchCurrencies(request: FetchCurrency? = null): CurrenciesCommand?
    suspend fun fetchAllCurrencies(): List<CurrencyData>
    suspend fun fetchCurrency(request: FetchCurrency): CurrencyCommand?
    suspend fun updateCurrency(request: CurrencyCommand): CurrencyCommand?
    suspend fun deleteCurrency(request: FetchCurrency): Void?
    suspend fun fetchAllCurrenciesPrecision(): List<CurrencyPrecision>
    suspend fun fetchCurrencyMaxOrder(symbol: String): BigDecimal?
    suspend fun fetchCurrencyLocalizations(symbol: String): List<CurrencyLocalizationCommand>
    suspend fun saveCurrencyLocalizations(
        symbol: String,
        localizations: List<CurrencyLocalizationCommand>
    ): List<CurrencyLocalizationCommand>

    suspend fun deleteCurrencyLocalization(id: Long)

}