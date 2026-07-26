package co.nilin.opex.profile.core.data.otp

data class NewOTPRequest(
    val userId: String,
    val receivers: List<OTPReceiver>,
    val action: String?
)
