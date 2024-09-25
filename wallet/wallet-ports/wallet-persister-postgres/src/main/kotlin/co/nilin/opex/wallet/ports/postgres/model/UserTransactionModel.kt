package co.nilin.opex.wallet.ports.postgres.model

import co.nilin.opex.wallet.core.model.UserTransactionCategory
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Table("user_transaction")
data class UserTransactionModel(
    val ownerId: Long,
    val txId: Long,
    val currency: String,
    val balance: BigDecimal,
    val balanceChange: BigDecimal,
    val category: UserTransactionCategory,
    val description: String? = null,
    val uuid: String = UUID.randomUUID().toString(),
    val date: LocalDateTime = LocalDateTime.now(),
    @Id
    val id: Long? = null
)
