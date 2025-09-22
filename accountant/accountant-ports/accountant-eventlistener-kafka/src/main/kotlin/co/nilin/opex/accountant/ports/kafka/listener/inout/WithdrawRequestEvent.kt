package co.nilin.opex.accountant.ports.kafka.listener.inout

import co.nilin.opex.accountant.core.model.WithdrawStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class WithdrawRequestEvent(
    val uuid: String,
    val withdrawId: Long? = null,
    val currency: String,
    val amount: BigDecimal,
    val status: WithdrawStatus,
    val createDate: LocalDateTime,
)
