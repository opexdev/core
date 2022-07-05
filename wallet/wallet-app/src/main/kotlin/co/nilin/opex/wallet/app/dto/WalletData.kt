package co.nilin.opex.wallet.app.dto

import java.math.BigDecimal

data class WalletData(
    val asset: String,
    var balance: BigDecimal,
    var locked: BigDecimal,
    var withdraw: BigDecimal
)
