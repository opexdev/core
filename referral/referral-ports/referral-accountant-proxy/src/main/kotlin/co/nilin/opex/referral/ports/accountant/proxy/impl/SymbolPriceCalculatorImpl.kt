package co.nilin.opex.referral.ports.accountant.proxy.impl

import co.nilin.opex.referral.core.api.SymbolPriceCalculator
import co.nilin.opex.referral.core.spi.AccountantProxy
import co.nilin.opex.referral.core.spi.ConfigHandler
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class SymbolPriceCalculatorImpl(
    private val accountantProxy: AccountantProxy,
    private val configHandler: ConfigHandler
) :
    SymbolPriceCalculator {
    override suspend fun getPrice(symbol: String): BigDecimal {
        val out = configHandler.findConfig("default")!!.paymentCurrency
        return accountantProxy.fetchLastPrice("$symbol$out") ?: accountantProxy.fetchLastPrice("$out$symbol")
            ?.takeIf { it > BigDecimal.ZERO }
            ?.let { BigDecimal.ONE / it } ?: BigDecimal.ZERO
    }
}