package co.nilin.opex.wallet.app.dto

import co.nilin.opex.common.OpexError
import java.math.BigDecimal

class SetCurrencyExchangeRateRequest(
    val sourceSymbol: String,
    val destSymbol: String,
    val rate: BigDecimal,
    var ignoreIfExist: Boolean? = false

) {

    fun validate() {
        if (rate <= BigDecimal.ZERO)
            throw OpexError.InvalidRate.exception()
        else if (sourceSymbol == destSymbol)
            throw OpexError.SourceIsEqualDest.exception()
    }
}