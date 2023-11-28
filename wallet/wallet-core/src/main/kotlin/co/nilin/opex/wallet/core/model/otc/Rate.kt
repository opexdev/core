package co.nilin.opex.wallet.core.model.otc

import java.math.BigDecimal

data class Rate(
        val sourceSymbol: String, val destSymbol: String, val rate: BigDecimal
)