package co.nilin.opex.wallet.core.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Withdraw(
    val withdrawId: Long? = null,
    val ownerUuid: String,
    val currency: String,
    val wallet: Long,
    val amount: BigDecimal,
    val requestTransaction: String,
    val finalizedTransaction: String?,
    val appliedFee: BigDecimal,
    val destAmount: BigDecimal?,
    val destSymbol: String?,
    val destAddress: String?,
    val destNetwork: String?,
    var destNote: String?,
    var destTransactionRef: String?,
    val statusReason: String?,
    var status: WithdrawStatus,
    var applicator: String?,
    val createDate: LocalDateTime = LocalDateTime.now(),
    val lastUpdateDate: LocalDateTime? = null
) {

    fun canBeProcessed(): Boolean {
        return status == WithdrawStatus.CREATED
    }

    fun canBeAccepted(): Boolean {
        return status == WithdrawStatus.CREATED || status == WithdrawStatus.PROCESSING
    }

    fun canBeCanceled(): Boolean {
        return status == WithdrawStatus.CREATED
    }

    fun canBeRejected(): Boolean {
        return status == WithdrawStatus.CREATED || status == WithdrawStatus.PROCESSING
    }
}