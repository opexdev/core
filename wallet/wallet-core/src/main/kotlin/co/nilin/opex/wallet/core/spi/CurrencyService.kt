package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.model.Currency

interface CurrencyService {

    suspend fun getCurrency(symbol: String): Currency?

    suspend fun addCurrency(name: String, symbol: String, precision: Double)

    suspend fun editCurrency(name: String, symbol: String, precision: Double)

    suspend fun deleteCurrency(name: String)
}