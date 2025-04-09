package co.nilin.opex.otp.app.data

data class VerifyTOTPRequest(
    val userId: String,
    val code: String
)