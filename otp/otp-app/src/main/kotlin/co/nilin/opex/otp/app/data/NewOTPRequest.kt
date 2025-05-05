package co.nilin.opex.otp.app.data

data class NewOTPRequest(
    val userId: String,
    val receivers: List<OTPReceiver>
)
