package co.nilin.opex.accountant.ports.kafka.listener.inout

import co.nilin.opex.accountant.core.model.FinancialActionStatus

data class FinancialActionResponseEvent(
    val uuid: String,
    val status: FinancialActionStatus,
    val reason:String?
)