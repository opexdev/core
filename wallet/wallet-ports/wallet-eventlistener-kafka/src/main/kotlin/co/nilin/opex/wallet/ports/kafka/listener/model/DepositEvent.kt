package co.nilin.opex.wallet.ports.kafka.listener.model

import co.nilin.opex.wallet.core.model.WithdrawStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class DepositEvent(
    val uuid: String,
    val depositRef: String? = null,
    val currency: String,
    val amount: BigDecimal,
    val createDate: LocalDateTime?= LocalDateTime.now(),
)