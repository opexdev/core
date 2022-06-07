package co.nilin.opex.utility.preferences

data class ChainSyncSchedule(
    var delay: Long = 600,
    var errorDelay: Long = 60,
    var timeout: Int = 30,
    var maxRetries: Int = 5,
    var confirmations: Int = 0
)
