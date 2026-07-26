package co.nilin.opex.wallet.core.model

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class UserTransaction(
    val ownerId: Long,
    val txId: Long,
    val currency: String,
    val balance: BigDecimal,
    val balanceChange: BigDecimal,
    val category: UserTransactionCategory,
    val description: String? = null,
    val uuid: String = UUID.randomUUID().toString(),
    val date: LocalDateTime = LocalDateTime.now()
)
