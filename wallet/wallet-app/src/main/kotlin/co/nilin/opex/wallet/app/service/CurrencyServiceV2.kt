package co.nilin.opex.wallet.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.CryptoCurrencyCommand
import co.nilin.opex.wallet.core.inout.CurrencyCommand
import co.nilin.opex.wallet.core.inout.WithdrawMethod
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.spi.CurrencyServiceManager
import org.apache.kafka.common.requests.FetchRequest
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

    suspend fun updateCurrency(request: CurrencyCommand): CurrencyCommand? {
        currencyServiceManager.fetchCurrency(FetchCurrency(uuid = request.uuid))
                ?.let {
                    return currencyServiceManager.updateCurrency(it.toUpdate(request))
                }?: throw OpexError.CurrencyNotFound.exception()
    }


    @Transactional
    suspend fun addImp2Currency(request: CryptoCurrencyCommand): CurrencyCommand? {
        return currencyServiceManager.fetchCurrency(FetchCurrency(uuid = request.currencyUUID))?.let {
            if (!it.isCryptoCurrency!!)
                updateCurrency(it.apply { isCryptoCurrency = true })
            currencyServiceManager.currency2Crypto(
                    request.apply {
                        currencyImpUuid = UUID.randomUUID().toString()
                    }
            )
        } ?: throw OpexError.CurrencyNotFound.exception()
    }


}