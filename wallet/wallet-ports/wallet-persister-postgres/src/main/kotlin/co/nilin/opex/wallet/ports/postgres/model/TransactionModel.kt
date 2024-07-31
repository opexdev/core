package co.nilin.opex.wallet.ports.postgres.model

import co.nilin.opex.wallet.core.model.TransferCategory
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("transaction")
data class TransactionModel(
    @Id var id: Long?,
    val sourceWallet: Long,
    val destWallet: Long,
    val sourceAmount: BigDecimal,
    val destAmount: BigDecimal,
    val description: String?,
    val transferRef: String?,
    val transferCategory: TransferCategory = TransferCategory.NONE,
    val transactionDate: LocalDateTime
)