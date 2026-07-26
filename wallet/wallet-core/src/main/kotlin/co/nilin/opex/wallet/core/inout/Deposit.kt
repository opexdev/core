package co.nilin.opex.wallet.core.inout

import co.nilin.opex.wallet.core.model.DepositStatus
import co.nilin.opex.wallet.core.model.DepositType
import java.math.BigDecimal
import java.util.*

data class Deposit(
    var ownerUuid: String,
    var depositUuid: String,
    var currency: String,
    var amount: BigDecimal,
    var acceptedFee: BigDecimal? = null,
    var appliedFee: BigDecimal? = null,
    var sourceSymbol: String? = null,
    var network: String? = null,
    var sourceAddress: String? = null,
    var transactionRef: String? = null,
    var note: String? = null,
    var status: DepositStatus,
    var depositType: DepositType,
    var attachment: String?,
    var createDate: Date = Date(),
    val id: Long? = null,
    var transferMethod: TransferMethod?,
)

data class Deposits(var deposits: List<Deposit>)