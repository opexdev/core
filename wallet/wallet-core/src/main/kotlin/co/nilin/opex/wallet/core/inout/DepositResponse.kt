package co.nilin.opex.wallet.core.inout

import co.nilin.opex.wallet.core.model.DepositStatus
import co.nilin.opex.wallet.core.model.DepositType
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Date

data class DepositResponse(
    val id: Long,
    val uuid: String,
    val currency: String,
    val amount: BigDecimal,
    val network: String?,
    val note: String?,
    val transactionRef: String?,
    val sourceAddress:String?,
    val status: DepositStatus,
    val type: DepositType,
    val attachment:String?,
    val createDate: Date?
)