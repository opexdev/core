package co.nilin.opex.wallet.core.inout

import co.nilin.opex.wallet.core.model.WithdrawStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class WithdrawResponse(
    val withdrawId: Long,
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
    val createDate: LocalDateTime,
    val lastUpdateDate: LocalDateTime?
)