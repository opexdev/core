package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("transaction")
class TransactionModel(
    @Id var id: Long?,
    @Column("source_wallet") val sourceWallet: Long,
    @Column("dest_wallet") val destWallet: Long,
    @Column("source_amount") val sourceAmount: BigDecimal,
    @Column("dest_amount") val destAmount: BigDecimal,
    val description: String?,
    @Column("transfer_ref")  val transferRef: String?,
    @Column("transaction_date") val txDate: LocalDateTime
)