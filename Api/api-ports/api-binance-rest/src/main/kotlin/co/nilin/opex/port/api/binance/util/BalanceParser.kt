package co.nilin.opex.port.api.binance.util

import co.nilin.opex.api.core.inout.Wallet
import co.nilin.opex.port.api.binance.data.BalanceResponse
import java.math.BigDecimal

object BalanceParser {

    fun parse(list: List<Wallet>): List<BalanceResponse> {
        val result = arrayListOf<BalanceResponse>()

        for (w in list) {
            result.addOrGet(w.asset).apply {
                when (w.type) {
                    "exchange" -> locked = w.balance
                    "main" -> free = w.balance
                }
            }
        }
        return result
    }

    private fun ArrayList<BalanceResponse>.addOrGet(symbol: String): BalanceResponse {
        for (w in this)
            if (w.asset == symbol)
                return w

        add(BalanceResponse(symbol, BigDecimal.ZERO, BigDecimal.ZERO))
        return this.last()
    }

}