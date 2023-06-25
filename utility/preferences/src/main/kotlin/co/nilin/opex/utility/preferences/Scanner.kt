package co.nilin.opex.utility.preferences

data class Scanner(
    var url: String = "",
    var maxBlockRange: Int = 30,
    var delayOnRateLimit: Int = 300,
    var maxParallelCall: Int = 3
)
