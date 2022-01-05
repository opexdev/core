package co.nilin.opex.referral.core.service

import co.nilin.opex.referral.core.api.SymbolPriceCalculator
import org.springframework.stereotype.Service
import java.math.BigDecimal

//TODO Must be replaced with actual price calculator
@Service
class MockedSymbolPriceCalculator : SymbolPriceCalculator {
    override suspend fun getPrice(symbol: String): BigDecimal = BigDecimal.ONE
}