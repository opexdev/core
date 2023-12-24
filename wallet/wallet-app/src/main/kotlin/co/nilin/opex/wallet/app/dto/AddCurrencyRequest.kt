package co.nilin.opex.wallet.app.dto

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.math.BigDecimal

data class AddCurrencyRequest(
        var symbol: String,
        var name: String,
        var precision: BigDecimal,
        var title: String? = null,
        var alias: String? = null,
        var maxDeposit: BigDecimal? = BigDecimal.TEN,
        var minDeposit: BigDecimal? = BigDecimal.ZERO,
        var minWithdraw: BigDecimal? = BigDecimal.TEN,
        var maxWithdraw: BigDecimal? = BigDecimal.ZERO,
        var icon: String? = null,
)

