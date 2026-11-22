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
    val transferMethod: String? = null,
    val operationType: String? = null,
    val applicator: String? = null,
    val gatewayId: String? = null,
    val network: String? = null,
    val note: String? = null,
    val attachment: String? = null,
    val senderPartyId: String? = null,
    val receiverPartyId: String? = null,
    val senderWalletId: String? = null,
    val receiverWalletId: String? = null,
    val senderWalletType: String? = null,
    val receiverWalletType: String? = null,
    val destinationAddress: String? = null,
    val destinationAsset: String? = null,
    val destinationAmount: BigDecimal? = null,
    val statusReason: String? = null,
    val requestTransactionId: String? = null,
    val finalizedTransactionId: String? = null,
    val operationWalletId: String? = null,
    val otpRequired: Int? = null
)