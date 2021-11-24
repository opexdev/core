package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.model.Currency

interface CurrencyService {
    suspend fun getCurrency(symbol: String): Currency?
}