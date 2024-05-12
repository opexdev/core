package co.nilin.opex.wallet.core.inout

data class FinancialActionResponseEvent(
    val uuid: String,
    var status: Status,
    var errorCode: Int? = null,
    var reason: String? = null
)

enum class Status { PROCESSED, ERROR }