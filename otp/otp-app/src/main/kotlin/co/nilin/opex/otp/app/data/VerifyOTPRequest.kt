package co.nilin.opex.otp.app.data

data class VerifyOTPRequest(
    val userId: String,
    val otpCodes: List<OTPCode>
)
