package co.nilin.opex.api.core.inout

class WithdrawActionResult(
    val withdrawId: String, val status: WithdrawStatus, val nextAction: WithdrawNextAction? = null
)

enum class WithdrawNextAction() {
    OTP_MOBILE,
    OTP_EMAIL,
    WAITING_FOR_ADMIN;
}