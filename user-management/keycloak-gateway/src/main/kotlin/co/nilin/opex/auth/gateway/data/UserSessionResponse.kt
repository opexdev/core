package co.nilin.opex.auth.gateway.data

data class UserSessionResponse(
    val ipAddress: String,
    val started: Int,
    val lastAccess: Int,
    val state: String,
)