package co.nilin.opex.api.core.inout.otc

data class ForbiddenPair(
    val sourceSymbol: String,
    val destinationSymbol: String
)

data class ForbiddenPairs(
    var forbiddenPairs: List<ForbiddenPair>?
)

data class ForbiddenSwapPair(
    val sourceSymbol: String,
    val destinationSymbol: String
)

data class ForbiddenSwapPairs(
    var forbiddenSwapPairs: List<ForbiddenSwapPair>?
)
