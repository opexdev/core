package co.nilin.opex.wallet.core.inout

import co.nilin.opex.wallet.core.model.WithdrawStatus
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class WithdrawActionResult(
    val withdrawId: Long,
    val status: WithdrawStatus,
    val nextAction: WithdrawNextAction? = null
) {
}

enum class WithdrawNextAction(val order: Int) {
    OTP_EMAIL(1),
    OTP_MOBILE(2),
    WAITING_FOR_ADMIN(3);

    companion object {
        fun fromOrder(order: Int): WithdrawNextAction? {
            return values().find { it.order == order }
        }
    }
}