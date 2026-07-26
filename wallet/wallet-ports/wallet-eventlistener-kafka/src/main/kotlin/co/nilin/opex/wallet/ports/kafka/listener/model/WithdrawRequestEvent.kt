package co.nilin.opex.wallet.ports.kafka.listener.model

import co.nilin.opex.wallet.core.model.WithdrawStatus
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