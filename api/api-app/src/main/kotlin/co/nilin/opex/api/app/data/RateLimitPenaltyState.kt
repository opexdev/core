package co.nilin.opex.api.app.data

data class RateLimitPenaltyState(
    var violationCount: Int = 0,
    var lastViolationAt: Long? = null,
    var bannedUntil: Long? = null
)