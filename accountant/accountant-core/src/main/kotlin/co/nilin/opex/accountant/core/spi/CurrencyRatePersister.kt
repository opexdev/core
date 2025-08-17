package co.nilin.opex.accountant.core.spi

import java.math.BigDecimal

interface CurrencyRatePersister {

    suspend fun updateRate(base: String, quote: String, rate: BigDecimal)
}