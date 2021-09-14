package co.nilin.opex.bcgateway.core.model

import java.math.BigDecimal

data class Deposit(
    val depositor: String,
    val depositorMemo: String?,
    val amount: BigDecimal,
    val chain: String?,
    val token: Boolean,
    val tokenAddress: String?
)
