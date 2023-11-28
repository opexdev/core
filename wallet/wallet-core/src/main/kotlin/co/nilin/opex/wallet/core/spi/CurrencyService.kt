package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.model.Currency
import java.math.BigDecimal

interface CurrencyService {

    suspend fun getCurrency(symbol: String): Currency?
    suspend fun addCurrency(name: String, symbol: String, precision: BigDecimal)
    suspend fun addCurrency(request: Currency): Currency?
    suspend fun updateCurrency(request: Currency): Currency?
    suspend fun editCurrency(name: String, symbol: String, precision: BigDecimal)
    suspend fun deleteCurrency(name: String): List<Currency>?

    suspend fun getCurrencies(): List<Currency>?
}