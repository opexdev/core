package co.nilin.mixchange.wallet.core.inout

import co.nilin.mixchange.wallet.core.model.Amount
import java.time.LocalDateTime

data class TransferResult(val date: LocalDateTime, val sourceBalanceBeforeAction: Amount, val sourceBalanceAfterAction: Amount, val amount: Amount)