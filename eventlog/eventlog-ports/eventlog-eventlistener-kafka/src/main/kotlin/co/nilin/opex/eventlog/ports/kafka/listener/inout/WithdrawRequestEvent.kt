package co.nilin.opex.eventlog.ports.kafka.listener.inout

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
    val status: String,
    val createDate: LocalDateTime,
    val lifecycleTransitionDate: LocalDateTime? = null,
    val completionDate: LocalDateTime? = null
)
