package co.nilin.opex.accountant.ports.kafka.listener.inout

import java.math.BigDecimal
import java.time.LocalDateTime

data class DepositEvent(
    val uuid: String,
    val depositRef: String? = null,
    val currency: String,
    val amount: BigDecimal,
    val createDate: LocalDateTime,
)
