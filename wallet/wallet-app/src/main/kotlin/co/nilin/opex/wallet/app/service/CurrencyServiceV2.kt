package co.nilin.opex.wallet.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.app.dto.CurrenciesDto
import co.nilin.opex.wallet.app.dto.CurrencyDto
import co.nilin.opex.wallet.app.utils.toDto
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

    suspend fun createNewCurrency(request: CurrencyDto): CurrencyDto? {
        return currencyServiceManager.createNewCurrency(
                request.apply {
                    uuid = UUID.randomUUID().toString()
                    symbol = symbol.uppercase()
                    isCryptoCurrency = false
                }.toCommand()
        )?.toDto()
    }

    suspend fun updateCurrency(request: CurrencyDto): CurrencyDto? {
        return currencyServiceManager.updateCurrency(request.toCommand())?.toDto()
    }


    @Transactional
    suspend fun addImp2Currency(request: CryptoCurrencyCommand): CurrencyDto? {
        return currencyServiceManager.prepareCurrencyToBeACryptoCurrency(request.currencyUUID)
                ?.apply {
                    impls =
                            cryptoCurrencyManager.toCryptoCurrency(
                                    request.apply {
                                        currencyImpUuid = UUID.randomUUID().toString()
                                    }
                            )?.imps
                }?.toDto()
    }


    suspend fun fetchCurrencyWithImps(currencyUUID: String, includeImpl: Boolean): CurrencyDto? {
        return currencyServiceManager.fetchCurrencies(FetchCurrency(uuid = currencyUUID))?.currencies?.get(0)
                ?.let {
                    if (it.isCryptoCurrency == true && includeImpl)
                        it.apply {
                            impls =
                                    cryptoCurrencyManager.fetchCurrencyImps(
                                            currencyUUID
                                    )?.imps
                        }.toDto()
                    else
                        it.toDto()
                } ?: throw OpexError.CurrencyNotFound.exception()
    }


    suspend fun fetchCurrenciesWithImps(includeImpl: Boolean): CurrenciesDto? {
        return CurrenciesDto(currencyServiceManager.fetchCurrencies(FetchCurrency())?.currencies?.stream()?.map {
            if (it.isCryptoCurrency == true && includeImpl)
                it.apply {
                    impls =
                            runBlocking {
                                cryptoCurrencyManager.fetchCurrencyImps(
                                        it.uuid!!
                                )?.imps
                            }
                }.toDto()
            else
                it.toDto()
        }?.collect(Collectors.toList()))
    }


    suspend fun updateImp(request: CryptoCurrencyCommand): CurrencyDto? {
        return currencyServiceManager.fetchCurrencies(FetchCurrency(uuid = request.currencyUUID))?.currencies?.get(0)
                ?.let {
                    if (it.isCryptoCurrency == true)
                        it.apply {
                            impls =
                                    cryptoCurrencyManager.updateCryptoImp(
                                            request
                                    )?.imps
                        }.toDto()
                    else
                        throw OpexError.ImpNotFound.exception()
                } ?: throw OpexError.CurrencyNotFound.exception()
    }


    suspend private fun fetchCurrencyImps(currencyUUID: String): CryptoImps? {
        return cryptoCurrencyManager.fetchCurrencyImps(currencyUUID)
    }

}