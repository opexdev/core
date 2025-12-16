package co.nilin.opex.wallet.app.utils

import co.nilin.opex.wallet.app.dto.WalletData
import co.nilin.opex.wallet.core.model.Wallet
import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.core.service.PrecisionService
import co.nilin.opex.wallet.ports.postgres.impl.PrecisionServiceImpl
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class BalanceParser(
    private val precisionService: PrecisionService
) {

    fun parse(list: List<Wallet>): List<WalletData> {
        val result = arrayListOf<WalletData>()

        for (w in list) {
            result.addOrGet(w.currency.symbol).apply {
                when (w.type) {
                    WalletType.MAIN -> balance = precisionService.calculatePrecision(w.balance.amount, w.currency.symbol , false)
                    WalletType.EXCHANGE -> locked = precisionService.calculatePrecision(w.balance.amount, w.currency.symbol , false)
                    WalletType.CASHOUT -> withdraw = precisionService.calculatePrecision(w.balance.amount, w.currency.symbol , false)
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
                WalletType.MAIN -> result.balance = precisionService.calculatePrecision(w.balance.amount, w.currency.symbol, false)
                WalletType.EXCHANGE -> result.locked =
                    precisionService.calculatePrecision(w.balance.amount, w.currency.symbol, false)

                WalletType.CASHOUT -> result.withdraw =
                    precisionService.calculatePrecision(w.balance.amount, w.currency.symbol, false)
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