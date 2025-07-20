package co.nilin.opex.profile.core.data.otp

data class VerifyOTPRequest(
    val userId: String,
    val otpCodes: List<OTPCode>
)
