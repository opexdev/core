package co.nilin.opex.wallet.app.dto

import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.wallet.core.spi.CurrencyService
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal

class SetCurrencyExchangeRateRequest(
    val sourceSymbol: String,
    val destSymbol: String,
    val rate: BigDecimal

){

    fun validate(){
        if(rate<= BigDecimal.ZERO )
            throw OpexException(OpexError.InvalidRate)
        else if(sourceSymbol==destSymbol)
            throw OpexException(OpexError.SourceIsEqualDest)


    }
}