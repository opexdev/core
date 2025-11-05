package co.nilin.opex.api.core.inout

class WithdrawActionResult(
    val withdrawId: Long, val status: WithdrawStatus, val nextAction: WithdrawNextAction? = null
)

enum class WithdrawNextAction() {
    OTP_EMAIL,
    OTP_MOBILE,
    WAITING_FOR_ADMIN;
}