package co.nilin.opex.referral.ports.accountant.proxy.impl

import co.nilin.opex.referral.core.api.SymbolPriceCalculator
import co.nilin.opex.referral.core.spi.ApiProxy
import co.nilin.opex.referral.core.spi.ConfigHandler
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class SymbolPriceCalculatorImpl(
    private val apiProxy: ApiProxy,
    private val configHandler: ConfigHandler
) : SymbolPriceCalculator {
    override suspend fun getPrice(symbol: String): BigDecimal {
        val out = configHandler.findConfig("default")!!.paymentCurrency
        return if (out == symbol) BigDecimal.ONE else
            apiProxy.fetchLastPrice("$symbol$out") ?: apiProxy.fetchLastPrice("$out$symbol")
                ?.takeIf { it > BigDecimal.ZERO }
                ?.let { BigDecimal.ONE / it } ?: BigDecimal.ZERO
    }
}
