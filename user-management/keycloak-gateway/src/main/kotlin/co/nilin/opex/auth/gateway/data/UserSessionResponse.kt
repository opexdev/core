package co.nilin.opex.auth.gateway.data

data class UserSessionResponse(
    val id: String?,
    val ipAddress: String?,
    val started: Long,
    val lastAccess: Long,
    val state: String?,
    val agent: String?,
    val inUse: Boolean
)