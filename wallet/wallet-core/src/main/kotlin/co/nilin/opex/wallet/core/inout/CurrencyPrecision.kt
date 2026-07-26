package co.nilin.opex.wallet.core.inout


import java.math.BigDecimal

data class CurrencyPrecision(
    var symbol: String,
    var precision: BigDecimal,
)
