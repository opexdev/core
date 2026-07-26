package co.nilin.opex.api.core.inout

data class RateLimitEndpoint(
    val id: Long? = null,
    val url: String,
    val method: String,
    val groupId: Long,
    val priority: Int,
    val enabled: Boolean = true
)