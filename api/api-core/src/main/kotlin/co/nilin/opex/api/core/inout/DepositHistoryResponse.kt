package co.nilin.opex.api.core.inout

import java.math.BigDecimal
import java.util.*

data class DepositHistoryResponse(
    val id: String,
    val uuid: String,
    val currency: String,
    val amount: BigDecimal,
    val network: String?,
    val note: String?,
    val transactionRef: String?,
    val sourceAddress: String?,
    val status: DepositStatus,
    val type: DepositType,
    val attachment: String?,
    val createDate: Date?,
    val transferMethod: TransferMethod?
)

enum class DepositType {

    ON_CHAIN, OFF_CHAIN
}

enum class DepositStatus {

    PROCESSING, DONE, INVALID
}