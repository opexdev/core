package co.nilin.opex.api.core

data class ContactUpdateConfirmRequest(
    val email: String? = null,
    val mobile: String? = null,
    val otpCode: String,
)