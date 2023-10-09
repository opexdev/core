package co.nilin.opex.utility.preferences

data class ChainSyncSchedule(
    var workerType: String = "MAIN",
    var delay: Long = 600,
    var timeout: Int = 30,
    var maxRetries: Int = 5,
    var confirmations: Int = 0,
    var maxBlockCount: Int = 10,
    var enabled: Boolean = true
)
