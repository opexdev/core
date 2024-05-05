package co.nilin.opex.wallet.app.service

import co.nilin.opex.wallet.core.inout.CryptoCurrencyCommand
import co.nilin.opex.wallet.core.inout.CurrencyCommand
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.spi.CurrencyServiceManager
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CurrencyServiceV2(
        @Qualifier("newVersion") private val currencyServiceManager: CurrencyServiceManager,
) {

    suspend fun createNewCurrency(request: CurrencyCommand): CurrencyCommand? {
        return currencyServiceManager.createNewCurrency(
                request.apply {
                    uuid = UUID.randomUUID().toString()
                    symbol = symbol.uppercase()
                    isCryptoCurrency = false
                }
        )
    }

    suspend fun updateCurrency(request: CurrencyCommand): CurrencyCommand? {
        return currencyServiceManager.updateCurrency(request)
    }


    @Transactional
    suspend fun addImp2Currency(request: CryptoCurrencyCommand): CurrencyCommand? {
        currencyServiceManager.prepareCurrencyToBeACryptoCurrency(request.currencyUUID)
        return currencyServiceManager.currency2Crypto(
                request.apply {
                    currencyImpUuid = UUID.randomUUID().toString()
                }
        )
    }


}