package co.nilin.opex.wallet.core.inout

import co.nilin.opex.wallet.core.model.WithdrawStatus
import co.nilin.opex.wallet.core.model.WithdrawType
import java.math.BigDecimal
import java.time.LocalDateTime

class WithdrawAdminResponse(
    val withdrawId: Long,
    val uuid: String,
    val ownerName: String?,
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
    val otpRequired: Int?,
)