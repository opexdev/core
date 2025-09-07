package co.nilin.opex.accountant.ports.kafka.listener.inout

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

enum class WithdrawStatus {

    CREATED,
    PROCESSING,
    CANCELED,
    REJECTED,
    DONE
}