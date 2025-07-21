package co.nilin.opex.profile.app.dto

data class VerifyUpdateEmailRequest(
    val email: String,
    val otp: String,
)
