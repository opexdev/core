package co.nilin.opex.wallet.app.service.otc

import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.spi.CurrencyService
import org.springframework.stereotype.Service

@Service
class CurrencyService (private val currencyService: CurrencyService){

    suspend fun addCurrency(request:Currency){
        currencyService.addCurrency(request)
    }

}