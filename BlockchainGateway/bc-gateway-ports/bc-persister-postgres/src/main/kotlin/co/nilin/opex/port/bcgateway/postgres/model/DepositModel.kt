package co.nilin.opex.port.bcgateway.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("deposits")
data class DepositModel(
    @Id val id: Long?,
    val hash: String,
    @Column("wallet_record_id") val walletRecordId: Long?,
    val depositor: String,
    @Column("depositor_memo") val depositorMemo: String?,
    val amount: BigDecimal,
    val chain: String,
    val token: Boolean,
    val tokenAddress: String?
)
