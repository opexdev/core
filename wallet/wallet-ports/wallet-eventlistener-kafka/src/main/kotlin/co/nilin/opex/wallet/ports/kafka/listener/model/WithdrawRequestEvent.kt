package co.nilin.opex.wallet.ports.kafka.listener.model

import co.nilin.opex.wallet.core.model.WithdrawStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class WithdrawRequestEvent(
    val uuid: String,
    val withdrawId: Long? = null,
    val withdrawUuid: String? = null,
    val lifecycleEventId: String? = null,
    val walletTransactionRef: String? = null,
    val businessReference: String? = null,
    val counterpartyReferenceId: String? = null,
    val currency: String,
    val amount: BigDecimal,
    val principalAmount: BigDecimal? = null,
    val appliedCustomerFee: BigDecimal? = null,
    val feeAsset: String? = null,
    val transferCategory: String? = null,
    val status: WithdrawStatus,
    val createDate: LocalDateTime,
    val lifecycleTransitionDate: LocalDateTime? = null,
    val completionDate: LocalDateTime? = null,
)