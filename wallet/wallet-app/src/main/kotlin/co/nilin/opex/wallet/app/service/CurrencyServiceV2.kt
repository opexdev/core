package co.nilin.opex.wallet.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.CryptoCurrencyCommand
import co.nilin.opex.wallet.core.inout.CurrencyCommand
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.spi.CurrencyServiceManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CurrencyServiceV2(
        private val currencyServiceManager: CurrencyServiceManager,
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

    @Transactional
    suspend fun currency2Crypto(request: CryptoCurrencyCommand): CurrencyCommand? {
        currencyServiceManager.fetchCurrency(FetchCurrency(uuid = request.currencyUUID))?.let {
            if (!it.isCryptoCurrency!!)
                updateCurrency(it.apply { isCryptoCurrency = true })
             currencyServiceManager.currency2Crypto(
                    request.apply {
                        currencyImpUuid = UUID.randomUUID().toString()
                    }
            )
        } ?: throw OpexError.CurrencyNotFound.exception()
    }


    suspend fun updateCurrency(request: CurrencyCommand): CurrencyCommand {

        currencyServiceManager.updateCurrency(request)

    }


}