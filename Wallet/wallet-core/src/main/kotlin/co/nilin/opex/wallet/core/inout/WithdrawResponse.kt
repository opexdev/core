package co.nilin.opex.wallet.core.inout

import java.math.BigDecimal
import java.util.*

class WithdrawResponse(
    val withdrawId: Long,
    val ownerUuid: String,
    val requestDate: Date,
    val finalizedDate: Date?,
    val requestTransaction: String,
    val finalizedTransaction: String?,
    val acceptedFee: BigDecimal,
    val appliedFee: BigDecimal?,
    val amount: BigDecimal?,
    val netAmount: BigDecimal?,
    val destCurrency: String?,
    val destAddress: String?,
    val destNetwork: String?,
    var destNote: String?,
    var destTransactionRef: String?,
    val statusReason: String?,
    val status: String
)