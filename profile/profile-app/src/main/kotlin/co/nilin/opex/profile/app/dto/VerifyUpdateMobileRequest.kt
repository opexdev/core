package co.nilin.opex.profile.app.dto

data class VerifyUpdateMobileRequest(
    val mobile: String,
    val otp: String,
)
