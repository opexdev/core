package co.nilin.opex.admin.ports.auth.data

data class ImpersonateRequest(
    val clientId: String,
    val clientSecret: String,
    val userId: String
)