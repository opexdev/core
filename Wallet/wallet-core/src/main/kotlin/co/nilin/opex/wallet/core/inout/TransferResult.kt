package co.nilin.opex.wallet.core.inout

import co.nilin.opex.wallet.core.model.Amount
import java.time.LocalDateTime

data class TransferResult(val date: Long, val sourceBalanceBeforeAction: Amount, val sourceBalanceAfterAction: Amount, val amount: Amount)