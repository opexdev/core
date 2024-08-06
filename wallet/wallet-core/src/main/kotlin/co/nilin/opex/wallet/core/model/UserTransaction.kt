package co.nilin.opex.wallet.core.model

import java.time.LocalDateTime
import java.util.UUID

data class UserTransaction(
    val ownerId: Long,
    val txId: Long,
    val currency: String,
    val balanceChange: Double,
    val balanceBefore: Double,
    val category: UserTransactionCategory,
    val description: String? = null,
    val uuid: String = UUID.randomUUID().toString(),
    val date: LocalDateTime = LocalDateTime.now()
)
