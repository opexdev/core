package co.nilin.opex.bcgateway.app.service

import co.nilin.opex.bcgateway.core.spi.CurrencyLoader
import org.springframework.stereotype.Service

@Service
class AdminService(private val currencyLoader: CurrencyLoader) {

    suspend fun addCurrency(name: String, symbol: String) {
        currencyLoader.addCurrency(name, symbol)
    }

    suspend fun editCurrency(name: String, symbol: String) {
        currencyLoader.editCurrency(name, symbol)
    }

    suspend fun deleteCurrency(name: String) {
        currencyLoader.deleteCurrency(name)
    }

}