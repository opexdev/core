package co.nilin.opex.bcgateway.core.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Deposit(
        val id: Long?,
        val hash: String,
        val depositor: String,
        val depositorMemo: String?,
        val amount: BigDecimal,
        val chain: String,
        val token: Boolean,
        val tokenAddress: String?,

)