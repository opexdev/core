package co.nilin.opex.wallet.app.utils

import co.nilin.opex.wallet.app.dto.WalletData
import co.nilin.opex.wallet.core.model.Wallet
import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.ports.postgres.util.RedisCacheHelper
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class BalanceParser(
    private val redisCacheHelper: RedisCacheHelper,
) {

    fun parse(list: List<Wallet>): List<WalletData> {
        val result = arrayListOf<WalletData>()

        for (w in list) {
            result.addOrGet(w.currency.symbol).apply {
                when (w.type) {
                    WalletType.MAIN -> balance = w.balance.amount.withPrecision(w.currency.symbol)
                    WalletType.EXCHANGE -> locked = w.balance.amount.withPrecision(w.currency.symbol)
                    WalletType.CASHOUT -> withdraw = w.balance.amount.withPrecision(w.currency.symbol)
                }
            }
        }
        return result
    }

    fun parseSingleCurrency(list: List<Wallet>): WalletData? {
        if (list.isEmpty()) return null
        val symbol = list[0].currency.symbol
        val result = WalletData(symbol, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)

        for (w in list) {
            if (w.currency.symbol != symbol)
                throw IllegalStateException("Found multiple currencies while parsing for single")

            when (w.type) {
                WalletType.MAIN -> result.balance = w.balance.amount.withPrecision(w.currency.symbol)
                WalletType.EXCHANGE -> result.locked = w.balance.amount.withPrecision(w.currency.symbol)
                WalletType.CASHOUT -> result.withdraw = w.balance.amount.withPrecision(w.currency.symbol)
            }
        }
        return result
    }

    private fun ArrayList<WalletData>.addOrGet(symbol: String): WalletData {
        for (w in this)
            if (w.asset == symbol)
                return w

        add(WalletData(symbol, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO))
        return this.last()
    }

    private fun BigDecimal.withPrecision(symbol: String): BigDecimal {
        val precision = redisCacheHelper.get<BigDecimal>("$symbol-precision") ?: return this
        val precisionScale = precision.stripTrailingZeros().scale()
        val decimalPart = this.stripTrailingZeros().toPlainString().substringAfter('.', "")

        if (decimalPart.isEmpty()) return setScale(precisionScale, RoundingMode.DOWN)

        val zeroPrefixCount = decimalPart.indexOfFirst { it != '0' }.takeIf { it >= 0 } ?: 0
        val rest = decimalPart.drop(zeroPrefixCount)

        var nonZeroCount = 0
        var digitsToKeep = 0
        for (c in rest) {
            digitsToKeep++
            if (c != '0') nonZeroCount++
            if (nonZeroCount == 2) break
        }
        val totalScale = zeroPrefixCount + digitsToKeep

        return if (zeroPrefixCount > 0 && nonZeroCount > 0) {
            setScale(totalScale, RoundingMode.DOWN)
        } else {
            setScale(precisionScale, RoundingMode.DOWN)
        }
    }


}