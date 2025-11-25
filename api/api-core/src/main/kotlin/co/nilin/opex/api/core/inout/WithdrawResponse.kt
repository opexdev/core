package co.nilin.opex.api.core.inout

import java.math.BigDecimal
import java.time.LocalDateTime

class WithdrawResponse(
    val withdrawId: String,
    val uuid: String,
    val amount: BigDecimal,
    val currency: String,
    val appliedFee: BigDecimal,
    val destAmount: BigDecimal?,
    val destSymbol: String?,
    val destAddress: String?,
    val destNetwork: String?,
    var destNote: String?,
    var destTransactionRef: String?,
    val statusReason: String?,
    val status: WithdrawStatus,
    var applicator: String?,
    var withdrawType: WithdrawType,
    var attachment: String?,
    val createDate: LocalDateTime,
    val lastUpdateDate: LocalDateTime?,
    var transferMethod: TransferMethod?,
    val otpRequired: Int? = 0,
)
enum class WithdrawType {
    CARD_TO_CARD, SHEBA, ON_CHAIN, OFF_CHAIN
}