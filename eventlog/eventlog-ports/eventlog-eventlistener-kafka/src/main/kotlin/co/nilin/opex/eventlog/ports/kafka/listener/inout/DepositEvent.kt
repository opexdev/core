package co.nilin.opex.eventlog.ports.kafka.listener.inout

import java.math.BigDecimal
import java.time.LocalDateTime

data class DepositEvent(
    val uuid: String,
    val depositRef: String? = null,
    val depositUuid: String? = null,
    val lifecycleEventId: String? = null,
    val walletTransactionRef: String? = null,
    val businessReference: String? = null,
    val currency: String,
    val amount: BigDecimal,
    val principalAmount: BigDecimal? = null,
    val appliedCustomerFee: BigDecimal? = null,
    val feeAsset: String? = null,
    val transferCategory: String? = null,
    val status: String? = null,
    val createDate: LocalDateTime? = null,
    val lifecycleTransitionDate: LocalDateTime? = null,
    val completionDate: LocalDateTime? = null
)
