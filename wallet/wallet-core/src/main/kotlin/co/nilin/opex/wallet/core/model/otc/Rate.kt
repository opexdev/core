package co.nilin.opex.wallet.core.model.otc

import java.math.BigDecimal

data class Rate(
        val sourceSymbol: String, val destinationSymbol: String, val rate: BigDecimal, var sourceSymbolId: Long?=null, var destinationSymbolId: Long?=null
)

data class Rates(
        var rates: List<Rate>?
)