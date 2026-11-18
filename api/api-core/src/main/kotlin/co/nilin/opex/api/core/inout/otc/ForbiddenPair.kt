package co.nilin.opex.api.core.inout.otc

data class ForbiddenPair(
    val sourceSymbol: String,
    val destinationSymbol: String,
    val applicator: String? = null
)

data class ForbiddenPairs(
    var forbiddenPairs: List<ForbiddenPair>?
)

data class ForbiddenSwapPair(
    val sourceSymbol: String,
    val destinationSymbol: String,
    val applicator: String? = null
)

data class ForbiddenSwapPairs(
    var forbiddenSwapPairs: List<ForbiddenSwapPair>?
)
