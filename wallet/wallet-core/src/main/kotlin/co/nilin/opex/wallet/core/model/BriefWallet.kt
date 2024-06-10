package co.nilin.opex.wallet.core.model

import java.math.BigDecimal

data class BriefWallet(
    val id: Long?,
    val ownerId: Long,
    val balance: BigDecimal,
    val currency: String,
    val type: String
)