package co.nilin.opex.wallet.core.model

import co.nilin.opex.wallet.core.inout.TransferMethod
import java.math.BigDecimal
import java.time.LocalDateTime

data class Withdraw(
    val withdrawId: Long? = null,
    val ownerUuid: String,
    val currency: String,
    val wallet: Long,
    val amount: BigDecimal,
    var requestTransaction: String? = null,
    val finalizedTransaction: String? = null,
    val appliedFee: BigDecimal,
    val destAmount: BigDecimal? = null,
    val destSymbol: String? = null,
    val destAddress: String? = null,
    val destNetwork: String? = null,
    var destNote: String? = null,
    var destTransactionRef: String? = null,
    val statusReason: String? = null,
    var status: WithdrawStatus,
    var applicator: String? = null,
    var withdrawType: WithdrawType,
    var attachment: String? = null,
    var createDate: LocalDateTime = LocalDateTime.now(),
    var lastUpdateDate: LocalDateTime? = null,
    var transferMethod: TransferMethod? = null,
    val otpRequired: Int? = null,
) {

    fun canBeAccepted(): Boolean {
        return status == WithdrawStatus.CREATED
    }

    fun canBeCanceled(): Boolean {
        return status == WithdrawStatus.CREATED
    }

    fun canBeRejected(): Boolean {
        return status == WithdrawStatus.CREATED || status == WithdrawStatus.ACCEPTED
    }

    fun canBeDone(): Boolean {
        return status == WithdrawStatus.ACCEPTED
    }
}