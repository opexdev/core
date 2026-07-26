package co.nilin.opex.wallet.core.inout

import co.nilin.opex.wallet.core.model.DepositStatus
import co.nilin.opex.wallet.core.model.DepositType
import java.math.BigDecimal
import java.time.LocalDateTime

data class DepositAdminResponse(
    val id: String,
    val uuid: String,
    val ownerName: String?,
    val currency: String,
    val amount: BigDecimal,
    val network: String?,
    val note: String?,
    val transactionRef: String?,
    val sourceAddress: String?,
    val status: DepositStatus,
    val type: DepositType,
    val attachment: String?,
    val createDate: LocalDateTime,
    val transferMethod: TransferMethod?
)