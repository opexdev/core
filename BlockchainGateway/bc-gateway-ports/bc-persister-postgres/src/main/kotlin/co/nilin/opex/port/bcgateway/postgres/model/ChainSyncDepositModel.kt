package co.nilin.opex.port.bcgateway.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("chain_sync_deposits")
data class ChainSyncDepositModel(
    @Id val id: Long?,
    val depositor: String,
    val depositorMemo: String?,
    val amount: BigDecimal,
    val chain: String?,
    val token: Boolean,
    val tokenAddress: String?
)
