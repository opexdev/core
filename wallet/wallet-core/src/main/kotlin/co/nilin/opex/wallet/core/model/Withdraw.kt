package co.nilin.opex.wallet.core.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Withdraw(
    val withdrawId: Long? = null,
    val ownerUuid: String,
    val currency: Long,
    val wallet: Long,
    val amount: BigDecimal,
    val requestTransaction: String,
    val finalizedTransaction: String?,
    val acceptedFee: BigDecimal,
    val appliedFee: BigDecimal?,
    val destAmount: BigDecimal?,
    val destSymbol: String?,
    val destAddress: String?,
    val destNetwork: String?,
    var destNote: String?,
    var destTransactionRef: String?,
    val statusReason: String?,
    val status: String,
    val createDate: LocalDateTime = LocalDateTime.now(),
    val acceptDate: LocalDateTime? = null
)