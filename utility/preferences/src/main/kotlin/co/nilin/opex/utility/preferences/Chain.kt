package co.nilin.opex.utility.preferences

data class Chain(
    var name: String = "",
    var addressType: String = "",
    val scanners: List<Scanner> = emptyList(),
    var schedules: List<ChainSyncSchedule> = emptyList()
)
