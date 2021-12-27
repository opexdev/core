package co.nilin.opex.referral.core.api

import java.math.BigDecimal

interface SymbolPriceCalculator {
    suspend fun getPrice(symbol: String): BigDecimal
}