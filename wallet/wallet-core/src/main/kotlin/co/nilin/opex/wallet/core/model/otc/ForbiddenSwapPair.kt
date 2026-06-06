package co.nilin.opex.wallet.core.model.otc

data class ForbiddenSwapPair(
    val sourceSymbol: String, val destinationSymbol: String
)

data class ForbiddenSwapPairs(
    var forbiddenSwapPairs: List<ForbiddenSwapPair>?
)