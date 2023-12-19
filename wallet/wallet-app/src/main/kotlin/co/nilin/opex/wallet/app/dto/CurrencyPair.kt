package co.nilin.opex.wallet.app.dto

import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException

data class CurrencyPair(
    val sourceSymbol: String,
    val destSymbol: String
){
    fun validate(){
        if(sourceSymbol==destSymbol)
            throw OpexException(OpexError.SourceIsEqualDest)
    }
}