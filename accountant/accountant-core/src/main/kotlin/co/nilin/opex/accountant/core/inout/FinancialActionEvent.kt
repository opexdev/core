package co.nilin.opex.accountant.core.inout

import co.nilin.opex.accountant.core.model.WalletType
import java.math.BigDecimal
import java.time.LocalDateTime

data class FinancialActionEvent(
    val uuid: String,
    val symbol: String,
    val amount: BigDecimal,
    val sender: String,
    val senderWalletType: WalletType,
    val receiver: String,
    val receiverWalletType: WalletType,
    val createDate: LocalDateTime,
    val transferRef: String?,
    val description: String
)