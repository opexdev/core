package co.nilin.opex.utility.preferences

data class ChainSyncSchedule(
    var delay: Long = 600, var errorDelay: Long = 60
)
