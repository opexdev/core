package co.nilin.opex.otp.app.data

data class VerifyOTPRequest(
    val code: String,
    val tracingCode: String,
)
