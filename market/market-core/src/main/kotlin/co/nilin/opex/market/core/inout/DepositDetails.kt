package co.nilin.opex.market.core.inout

import java.math.BigDecimal

data class DepositDetails(
    val hash: String,
    val address: String,
    val memo: String?,
    val amount: BigDecimal,
    val chain: String,
    val isToken: Boolean,
    val tokenAddress: String?
)