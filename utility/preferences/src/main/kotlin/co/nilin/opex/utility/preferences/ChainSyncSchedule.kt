package co.nilin.opex.utility.preferences

data class ChainSyncSchedule(
    var delay: Long = 0,
    var errorDelay: Long = 0
)
