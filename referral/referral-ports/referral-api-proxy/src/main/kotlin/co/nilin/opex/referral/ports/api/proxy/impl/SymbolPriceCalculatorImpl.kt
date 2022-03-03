package co.nilin.opex.referral.ports.api.proxy.impl

import co.nilin.opex.referral.core.api.SymbolPriceCalculator
import co.nilin.opex.referral.core.spi.ApiProxy
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class SymbolPriceCalculatorImpl(private val apiProxy: ApiProxy) : SymbolPriceCalculator {
    override suspend fun getPrice(symbol: String, paymentCurrency: String): BigDecimal {
        return if (paymentCurrency == symbol) BigDecimal.ONE else
            apiProxy.fetchLastPrice("$symbol$paymentCurrency") ?: apiProxy.fetchLastPrice("$paymentCurrency$symbol")
                ?.takeIf { it > BigDecimal.ZERO }
                ?.let { BigDecimal.ONE / it } ?: BigDecimal.ZERO
    }
}
