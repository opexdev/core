package co.nilin.opex.wallet.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.app.dto.CurrenciesDto
import co.nilin.opex.wallet.app.dto.CurrencyDto
import co.nilin.opex.wallet.app.utils.toDto
import co.nilin.opex.wallet.core.inout.OnChainGatewayCommand
import co.nilin.opex.wallet.core.inout.CurrencyGatewayCommand
import co.nilin.opex.wallet.core.inout.CurrencyGateways
import co.nilin.opex.wallet.core.inout.GatewayType
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
    suspend fun addGateway2Currency(request: CurrencyGatewayCommand): CurrencyGatewayCommand? {


        currencyServiceManager.fetchCurrency(FetchCurrency(symbol = request.currencySymbol))
                ?: throw OpexError.CurrencyNotFound.exception()

        when (request.type) {

            GatewayType.OnChain -> {
                currencyServiceManager.prepareCurrencyToBeACryptoCurrency(request.currencySymbol!!)
                        ?: throw OpexError.BadRequest.exception()

                return cryptoCurrencyManager.createGateway(
                        request.apply {
                            gatewayUuid = UUID.randomUUID().toString()
                        }
                )
            }

            GatewayType.OffChain -> {

return  null
            }

            GatewayType.Manually -> {
return null

            }

        }

    }

    suspend fun fetchCurrencyWithGateways(currencySymbol: String, includeGateway: Boolean?): CurrencyDto? {
        return currencyServiceManager.fetchCurrency(FetchCurrency(symbol = currencySymbol))
                ?.let {
//                    if (it.isCryptoCurrency == true && includeGateway == true)
                    if (includeGateway == true)
                        it.apply {
                            gateways =
                                    cryptoCurrencyManager.fetchGateways(
                                            currencySymbol
                                    )?.gateways
                        }.toDto()
                    else
                        it.toDto()
                } ?: throw OpexError.CurrencyNotFound.exception()
    }


    suspend fun fetchCurrencyGateway(currencyGatewayUUID: String, currencySymbol: String): CurrencyGatewayCommand? {
        currencyServiceManager.fetchCurrency(FetchCurrency(symbol = currencySymbol))
                ?: throw OpexError.CurrencyNotFound.exception()
        return cryptoCurrencyManager.fetchGateway(currencyGatewayUUID, currencySymbol)
    }

    suspend fun deleteGateway(currencyGatewayUUID: String, currencySymbol: String) {
        currencyServiceManager.fetchCurrency(FetchCurrency(symbol = currencySymbol))
                ?: throw OpexError.CurrencyNotFound.exception()
        cryptoCurrencyManager.deleteGateway(currencyGatewayUUID, currencySymbol)


    }

    suspend fun fetchGateways(): CurrencyGateways? {
        return cryptoCurrencyManager.fetchGateways();
    }

    //todo
//     fetch all gateways in single request and then map the results together
    suspend fun fetchCurrenciesWithGateways(includeGateway: Boolean?): CurrenciesDto? {
        return CurrenciesDto(currencyServiceManager.fetchCurrencies()?.currencies?.stream()?.map {
//            if (it.isCryptoCurrency == true && includeGateway == true)
            if (includeGateway == true)
                it.apply {
                    gateways =
                            runBlocking {
                                cryptoCurrencyManager.fetchGateways(
                                        it.symbol!!
                                )?.gateways
                            }
                }.toDto()
            else
                it.toDto()
        }?.collect(Collectors.toList()))
    }


//    suspend fun fetchCurrenciesWithGateways(includeGateway: Boolean?): CurrenciesDto? {
//        var currencies = currencyServiceManager.fetchCurrencies()?.currencies
//        val currenciesGateways = cryptoCurrencyManager.fetchGateways()
//        val groupedByGateway = currenciesGateways?.imps?.groupBy { it.currencySymbol }
//        return CurrenciesDto(currencies?.map { it.apply { gateways = groupedByGateway?.get(it.symbol) }.toDto() }?.toList())
//
//    }


    suspend fun updateGateway(request: CurrencyGatewayCommand): CurrencyGatewayCommand? {
        currencyServiceManager.fetchCurrency(FetchCurrency(symbol = request.currencySymbol))
                ?.let {
//                    if (it.isCryptoCurrency == true)
                    return cryptoCurrencyManager.updateCryptoGateway(
                            request
                    )

//                    else
//                        throw OpexError.GatewayNotFound.exception()
                } ?: throw OpexError.CurrencyNotFound.exception()
    }


    private suspend fun fetchCurrencyImps(currencySymbol: String): CurrencyGateways? {
        return cryptoCurrencyManager.fetchGateways(currencySymbol)
    }

}