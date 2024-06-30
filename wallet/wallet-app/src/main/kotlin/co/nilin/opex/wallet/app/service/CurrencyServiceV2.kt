package co.nilin.opex.wallet.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.app.dto.CurrenciesDto
import co.nilin.opex.wallet.app.dto.CurrencyDto
import co.nilin.opex.wallet.app.utils.toDto
import co.nilin.opex.wallet.core.inout.CryptoCurrencyCommand
import co.nilin.opex.wallet.core.inout.CryptoImps
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.service.CryptoCurrencyService
import co.nilin.opex.wallet.core.spi.CurrencyServiceManager
import co.nilin.opex.wallet.core.spi.WalletManager
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import java.util.stream.Collectors

@Service
class CurrencyServiceV2(
        @Qualifier("newVersion") private val currencyServiceManager: CurrencyServiceManager,
        private val cryptoCurrencyManager: CryptoCurrencyService,
        private val walletManager: WalletManager
) {

    suspend fun createNewCurrency(request: CurrencyDto): CurrencyDto? {
        val nc = currencyServiceManager.createNewCurrency(
                request.apply {
                    uuid = UUID.randomUUID().toString()
                    symbol = symbol?.uppercase() ?: throw OpexError.BadRequest.exception()
                    isCryptoCurrency = false
                }.toCommand()
        )?.toDto()
        walletManager.createWalletForSystem(request.symbol!!)
        return nc
    }

    suspend fun updateCurrency(request: CurrencyDto): CurrencyDto? {
        currencyServiceManager.fetchCurrency(FetchCurrency(symbol = request.symbol))
                ?: throw OpexError.CurrencyNotFound.exception()
        return currencyServiceManager.updateCurrency(request.toCommand())?.toDto()
    }


    @Transactional
    suspend fun addImp2Currency(request: CryptoCurrencyCommand): CryptoCurrencyCommand? {
        currencyServiceManager.prepareCurrencyToBeACryptoCurrency(request.currencySymbol
                ?: throw OpexError.BadRequest.exception())

        return cryptoCurrencyManager.createImpl(
                request.apply {
                    implUuid = UUID.randomUUID().toString()
                }
        )
    }


    suspend fun fetchCurrencyWithImpls(currencySymbol: String, includeImpl: Boolean?): CurrencyDto? {
        return currencyServiceManager.fetchCurrency(FetchCurrency(symbol = currencySymbol))
                ?.let {
//                    if (it.isCryptoCurrency == true && includeImpl == true)
                    if (includeImpl == true)
                        it.apply {
                            impls =
                                    cryptoCurrencyManager.fetchImpls(
                                            currencySymbol
                                    )?.imps
                        }.toDto()
                    else
                        it.toDto()
                } ?: throw OpexError.CurrencyNotFound.exception()
    }


    suspend fun fetchCurrencyImpl(currencyImplUUID: String, currencySymbol: String): CryptoCurrencyCommand? {
        currencyServiceManager.fetchCurrency(FetchCurrency(symbol = currencySymbol))
                ?: throw OpexError.CurrencyNotFound.exception()
        return cryptoCurrencyManager.fetchImpl(currencyImplUUID, currencySymbol)
    }

    suspend fun deleteImpl(currencyImplUUID: String, currencySymbol: String) {
        currencyServiceManager.fetchCurrency(FetchCurrency(symbol = currencySymbol))
                ?: throw OpexError.CurrencyNotFound.exception()
        cryptoCurrencyManager.deleteImpl(currencyImplUUID, currencySymbol)


    }

    suspend fun fetchImpls(): CryptoImps? {
        return cryptoCurrencyManager.fetchImpls();
    }


    //todo
    // fetch all impls in single request and then map the results together
    suspend fun fetchCurrenciesWithImpls(includeImpl: Boolean?): CurrenciesDto? {
        return CurrenciesDto(currencyServiceManager.fetchCurrencies()?.currencies?.stream()?.map {
//            if (it.isCryptoCurrency == true && includeImpl == true)
            if (includeImpl == true)
                it.apply {
                    impls =
                            runBlocking {
                                cryptoCurrencyManager.fetchImpls(
                                        it.symbol!!
                                )?.imps
                            }
                }.toDto()
            else
                it.toDto()
        }?.collect(Collectors.toList()))
    }


    suspend fun updateImpl(request: CryptoCurrencyCommand): CryptoCurrencyCommand? {
        currencyServiceManager.fetchCurrency(FetchCurrency(symbol = request.currencySymbol))
                ?.let {
//                    if (it.isCryptoCurrency == true)
                    return cryptoCurrencyManager.updateCryptoImpl(
                            request
                    )

//                    else
//                        throw OpexError.ImplNotFound.exception()
                } ?: throw OpexError.CurrencyNotFound.exception()
    }


    private suspend fun fetchCurrencyImps(currencySymbol: String): CryptoImps? {
        return cryptoCurrencyManager.fetchImpls(currencySymbol)
    }

}