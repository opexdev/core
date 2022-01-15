package co.nilin.opex.referral.ports.accountant.proxy.impl

import co.nilin.opex.referral.core.api.SymbolPriceCalculator
import co.nilin.opex.referral.core.spi.AccountantProxy
import co.nilin.opex.referral.core.spi.ConfigHandler
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class SymbolPriceCalculator(private val accountantProxy: AccountantProxy, private val configHandler: ConfigHandler) :
    SymbolPriceCalculator {
    override suspend fun getPrice(symbol: String): BigDecimal {
        val out = configHandler.findConfig("default")!!.paymentCurrency
        val pair = accountantProxy.fetchPairConfigs()
            .find { (it.leftSideWalletSymbol == symbol && it.rightSideWalletSymbol == out) || (it.leftSideWalletSymbol == out && it.rightSideWalletSymbol == symbol) }
            ?: return BigDecimal.ZERO
        if (pair.rate.isNaN()) return BigDecimal.ZERO
        return if (symbol == pair.leftSideWalletSymbol) pair.rate.toBigDecimal() else BigDecimal.ONE / pair.rate.toBigDecimal()
    }
}
