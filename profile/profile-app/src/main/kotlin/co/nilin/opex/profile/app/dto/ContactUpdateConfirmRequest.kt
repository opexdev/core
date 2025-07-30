package co.nilin.opex.profile.app.dto

data class ContactUpdateConfirmRequest(
    val email: String? = null,
    val mobile: String? = null,
    val otpCode: String,
)