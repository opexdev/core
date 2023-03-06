package co.nilin.opex.accountant.core.inout

import java.math.BigDecimal
import java.time.LocalDateTime

data class FinancialActionEvent(
    val uuid: String,
    val symbol: String,
    val amount: BigDecimal,
    val sender: String,
    val senderWalletType: String,
    val receiver: String,
    val receiverWalletType: String,
    val createDate: LocalDateTime,
    val transferRef: String?,
    val description: String
)