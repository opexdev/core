package co.nilin.opex.wallet.ports.postgres.model

import co.nilin.opex.wallet.core.model.WithdrawStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("withdraws")
data class WithdrawModel(
    @Id var id: Long?,
    @Column("uuid") val ownerUuid: String,
    val currency: String,
    val wallet: Long,
    val amount: BigDecimal,
    @Column("req_transaction_id")
    val requestTransaction: String,
    @Column("final_transaction_id")
    val finalizedTransaction: String?,
    val appliedFee: BigDecimal,
    val destAmount: BigDecimal?,
    val destSymbol: String?,
    val destNetwork: String?,
    val destAddress: String?,
    var destNotes: String?,
    var destTransactionRef: String?,
    var statusReason: String?,
    var status: WithdrawStatus,
    val createDate: LocalDateTime = LocalDateTime.now(),
    val acceptDate: LocalDateTime? = null
)