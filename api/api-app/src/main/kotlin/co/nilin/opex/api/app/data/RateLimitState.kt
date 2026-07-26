package co.nilin.opex.api.app.data

data class RateLimitState(
    var violationCount: Int = 0,
    var blockedUntil: Long? = null,
    var lastViolationAt: Long? = null,
    var graceRemaining: Int = 0
)
