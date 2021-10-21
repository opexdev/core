package co.nilin.opex.wallet.core.model

import java.math.BigDecimal

class Withdraw(
    val withdrawId: Long? = null,
    val ownerUuid: String,
    val wallet: Long,
    val amount: BigDecimal,
    val requestTransaction: String,
    val finalizedTransaction: String?,
    val acceptedFee: BigDecimal,
    val appliedFee: BigDecimal?,
    val netAmount: BigDecimal?,
    val destCurrency: String?,
    val destAddress: String?,
    val destNetwork: String?,
    var destNote: String?,
    var destTransactionRef: String?,
    val statusReason: String?,
    val status: String
)