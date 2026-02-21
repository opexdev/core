package co.nilin.opex.api.core.inout

data class ContactUpdateRequest(
    val email: String? = null,
    val mobile: String? = null
)