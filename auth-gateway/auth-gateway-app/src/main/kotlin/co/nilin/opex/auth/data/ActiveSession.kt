package co.nilin.opex.auth.data

data class ActiveSession(
    val id: String,
    val username: String,
    val userId: String,
    val ip: String,
    val loginDate: Long,
    val lastAccessDate: Long,
    val client: String?,
    val current: Boolean = false
)