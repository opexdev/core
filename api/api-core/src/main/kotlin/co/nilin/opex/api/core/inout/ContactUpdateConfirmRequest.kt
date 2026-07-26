package co.nilin.opex.api.core.inout

data class ContactUpdateConfirmRequest(
    val email: String? = null,
    val mobile: String? = null,
    val otpCode: String,
)