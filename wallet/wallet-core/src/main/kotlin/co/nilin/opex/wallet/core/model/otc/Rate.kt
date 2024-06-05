package co.nilin.opex.wallet.core.model.otc

import java.math.BigDecimal

data class Rate(
        val sourceSymbol: Long, val destSymbol: Long, val rate: BigDecimal
)

data class Rates(
        var rates: List<Rate>?
)