package co.nilin.opex.wallet.app.utils

import co.nilin.opex.wallet.app.dto.WalletData
import co.nilin.opex.wallet.core.model.Wallet
import java.math.BigDecimal

object BalanceParser {

    fun parse(list: List<Wallet>): List<WalletData> {
        val result = arrayListOf<WalletData>()

        for (w in list) {
            result.addOrGet(w.currency.symbol).apply {
                when (w.type) {
                    "main" -> balance = w.balance.amount
                    "exchange" -> locked = w.balance.amount
                    "cashout" -> withdraw = w.balance.amount
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
                "main" -> result.balance = w.balance.amount
                "exchange" -> result.locked = w.balance.amount
                "cashout" -> result.withdraw = w.balance.amount
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

}