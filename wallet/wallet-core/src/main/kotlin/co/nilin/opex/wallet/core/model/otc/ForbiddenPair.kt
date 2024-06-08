package co.nilin.opex.wallet.core.model.otc

data class ForbiddenPair (
        val sourceSymbol: String, val destinationSymbol: String, var sourceSymbolId:Long?=null, var destinationSymbolId:Long?=null
)

data class ForbiddenPairs (
       var forbiddenPairs:List<ForbiddenPair>?
)