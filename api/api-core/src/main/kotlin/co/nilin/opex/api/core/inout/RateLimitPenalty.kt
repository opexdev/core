package co.nilin.opex.api.core.inout

data class RateLimitPenalty(
    val id: Long? = null,
    val groupId: Long,
    val blockStep: Int,
    val blockDurationSeconds: Int
)
