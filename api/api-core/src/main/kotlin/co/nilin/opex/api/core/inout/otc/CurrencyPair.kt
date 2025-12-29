package co.nilin.opex.api.core.inout.otc

import co.nilin.opex.common.OpexError

data class CurrencyPair(
    val sourceSymbol: String,
    val destSymbol: String
) {
    fun validate() {
        if (sourceSymbol == destSymbol)
            throw OpexError.SourceIsEqualDest.exception()
    }
}
