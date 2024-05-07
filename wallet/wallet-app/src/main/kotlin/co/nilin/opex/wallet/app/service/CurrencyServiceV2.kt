package co.nilin.opex.wallet.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.CryptoCurrencyCommand
import co.nilin.opex.wallet.core.inout.CryptoImps
import co.nilin.opex.wallet.core.inout.CurrenciesCommand
import co.nilin.opex.wallet.core.inout.CurrencyCommand
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.service.CryptoCurrencyService
import co.nilin.opex.wallet.core.spi.CurrencyServiceManager
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Collections
import java.util.UUID
import java.util.stream.Collectors

@Service
class CurrencyServiceV2(
        @Qualifier("newVersion") private val currencyServiceManager: CurrencyServiceManager,
        private val cryptoCurrencyManager: CryptoCurrencyService,
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
        return currencyServiceManager.prepareCurrencyToBeACryptoCurrency(request.currencyUUID)
                ?.apply {
                    impls =
                            cryptoCurrencyManager.toCryptoCurrency(
                                    request.apply {
                                        currencyImpUuid = UUID.randomUUID().toString()
                                    }
                            )?.imps
                }
    }


    suspend fun fetchCurrencyWithImps(currencyUUID: String): CurrencyCommand? {
        return currencyServiceManager.fetchCurrencies(FetchCurrency(uuid = currencyUUID))?.currencies?.get(0)
                ?.let {
                    if (it.isCryptoCurrency == true)
                        it.apply {
                            impls =
                                    cryptoCurrencyManager.fetchCurrencyImps(
                                            currencyUUID
                                    )?.imps
                        }
                    else
                        it
                } ?: throw OpexError.CurrencyNotFound.exception()
    }


    suspend fun fetchCurrenciesWithImps(): CurrenciesCommand? {
        return CurrenciesCommand(currencyServiceManager.fetchCurrencies(FetchCurrency())?.currencies?.stream()?.map {
            if (it.isCryptoCurrency == true)
                it.apply {
                    impls =
                            runBlocking {
                                cryptoCurrencyManager.fetchCurrencyImps(
                                        it.uuid!!
                                )?.imps
                            }
                }
            else
                it
        }?.collect(Collectors.toList()))
    }


    suspend fun updateImp(request: CryptoCurrencyCommand): CurrencyCommand? {
        return currencyServiceManager.fetchCurrencies(FetchCurrency(uuid = request.currencyUUID))?.currencies?.get(0)
                ?.let {
                    if (it.isCryptoCurrency == true)
                        it.apply {
                            impls =
                                    cryptoCurrencyManager.updateCryptoImp(
                                            request
                                    )?.imps
                        }
                    else
                        throw OpexError.ImpNotFound.exception()
                } ?: throw OpexError.CurrencyNotFound.exception()
    }


    suspend fun fetchCurrencyImps(currencyUUID: String): CryptoImps? {
        return cryptoCurrencyManager.fetchCurrencyImps(currencyUUID)
    }

}