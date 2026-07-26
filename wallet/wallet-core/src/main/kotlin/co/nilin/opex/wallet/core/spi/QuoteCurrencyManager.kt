package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.model.QuoteCurrency

interface QuoteCurrencyManager {

    suspend fun getAll(isReference: Boolean?): List<QuoteCurrency>
    suspend fun update(currency: String, isReference: Boolean, displayOrder: Int)
}