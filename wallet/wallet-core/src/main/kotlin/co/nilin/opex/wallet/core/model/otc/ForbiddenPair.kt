package co.nilin.opex.wallet.core.model.otc

data class ForbiddenPair(
    val sourceSymbol: String, val destinationSymbol: String
)

data class ForbiddenPairs(
    var forbiddenPairs: List<ForbiddenPair>?
)