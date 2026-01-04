package co.nilin.opex.api.core

data class ContactUpdateRequest(
    val email: String? = null,
    val mobile: String? = null
)