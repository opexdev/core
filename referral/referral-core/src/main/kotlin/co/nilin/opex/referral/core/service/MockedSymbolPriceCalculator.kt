package co.nilin.opex.referral.core.service

import co.nilin.opex.referral.core.api.SymbolPriceCalculator
import java.math.BigDecimal

// TODO Must be replaced with actual price calculator
class MockedSymbolPriceCalculator : SymbolPriceCalculator {
    override suspend fun getPrice(symbol: String): BigDecimal = BigDecimal.ONE
}