package co.nilin.opex.utility.preferences

data class ChainSyncSchedule(var chain: String, var retryTime: Long, var delay: Long, var errorDelay: Long)
