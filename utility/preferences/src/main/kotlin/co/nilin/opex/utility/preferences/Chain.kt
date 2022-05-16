package co.nilin.opex.utility.preferences

data class Chain(
    var name: String = "",
    var addressType: String = "",
    val endpointUrl: String = "",
    var schedule: ChainSyncSchedule = ChainSyncSchedule()
)
