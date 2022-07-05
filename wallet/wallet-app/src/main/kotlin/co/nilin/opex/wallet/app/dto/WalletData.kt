package co.nilin.opex.wallet.app.dto

import java.math.BigDecimal

data class WalletData(
    val asset: String,
    val balance: BigDecimal,
    val type: String
)
