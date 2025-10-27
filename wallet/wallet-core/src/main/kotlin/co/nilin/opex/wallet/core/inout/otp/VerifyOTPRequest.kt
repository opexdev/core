package co.nilin.opex.wallet.core.inout.otp

data class VerifyOTPRequest(
    val userId: String,
    val otpCodes: List<OTPCode>
)
