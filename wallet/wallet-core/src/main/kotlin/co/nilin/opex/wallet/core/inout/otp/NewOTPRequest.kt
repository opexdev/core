package co.nilin.opex.wallet.core.inout.otp

data class NewOTPRequest(
    val userId: String,
    val receivers: List<OTPReceiver>,
    val action: String?
)
