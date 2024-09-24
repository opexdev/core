package co.nilin.opex.wallet.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.app.dto.CurrenciesDto
import co.nilin.opex.wallet.app.dto.CurrencyDto
import co.nilin.opex.wallet.app.utils.toDto
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.service.GatewayService
import co.nilin.opex.wallet.core.spi.CurrencyServiceManager
import co.nilin.opex.wallet.core.spi.WalletManager
import co.nilin.opex.wallet.ports.proxy.bcgateway.impl.OnChainGatewayProxyGateway
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.ArrayList
import java.util.UUID
import java.util.stream.Collectors

@Service
class CurrencyServiceV2(
        @Qualifier("newVersion") private val currencyServiceManager: CurrencyServiceManager,
        private val walletManager: WalletManager,
        private val gatewayService: GatewayService
) {
    private val logger = LoggerFactory.getLogger(CurrencyServiceManager::class.java)

    suspend fun createNewCurrency(request: CurrencyDto): CurrencyDto? {
        val nc = currencyServiceManager.createNewCurrency(
                request.apply {
                    uuid = UUID.randomUUID().toString()
                    symbol = symbol?.uppercase() ?: throw OpexError.BadRequest.exception()
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

//        if (request is OnChainGatewayCommand) {
//            currencyServiceManager.prepareCurrencyToBeACryptoCurrency(request.currencySymbol!!)
//                    ?: throw OpexError.BadRequest.exception()
//        }
        return gatewayService.createGateway(request);

    }

    suspend fun fetchCurrencyWithGateways(currencySymbol: String, includeGateways: List<GatewayType>? = null): CurrencyDto? {
        return currencyServiceManager.fetchCurrency(FetchCurrency(symbol = currencySymbol))
                ?.let { it ->
//                    if (it.isCryptoCurrency == true && includeGateway == true)
                    var gateways = gatewayService.fetchGateways(currencySymbol, includeGateways)
                    return it.apply {
                        it.depositAllowed = gateways?.stream()?.filter { it.isActive == true }?.map(CurrencyGatewayCommand::depositAllowed)?.reduce { t, u -> t ?: false || u ?: false }?.orElseGet { false }
                        it.withdrawAllowed = gateways?.stream()?.filter { it.isActive == true }?.map(CurrencyGatewayCommand::withdrawAllowed)?.reduce { t, u -> t ?: false || u ?: false }?.orElseGet { false }
                        it.gateways = gateways
                        //It is a stupid field for resolving front-end developers need
                        gateways?.forEach { gateway ->
                            run {
                                if (gateway is OnChainGatewayCommand) {
                                    it.availableGatewayType = GatewayType.OnChain.name;
                                } else if (gateway is OffChainGatewayCommand) {
                                    it.availableGatewayType = GatewayType.OffChain.name;
                                }
                            }
                        }

                    }.toDto()

                } ?: throw OpexError.CurrencyNotFound.exception()
    }

    suspend fun fetchCurrencyGateway(currencyGatewayUUID: String, currencySymbol: String): CurrencyGatewayCommand? {
        currencyServiceManager.fetchCurrency(FetchCurrency(symbol = currencySymbol))
                ?: throw OpexError.CurrencyNotFound.exception()
        return gatewayService.fetchGateway(currencyGatewayUUID, currencySymbol)
    }

    suspend fun deleteGateway(currencyGatewayUUID: String, currencySymbol: String) {
        currencyServiceManager.fetchCurrency(FetchCurrency(symbol = currencySymbol))
                ?: throw OpexError.CurrencyNotFound.exception()
        gatewayService.deleteGateway(currencyGatewayUUID, currencySymbol)


    }

    suspend fun fetchGateways(includes: List<GatewayType>): List<CurrencyGatewayCommand>? {
        return gatewayService.fetchGateways(null, includes)
    }


    suspend fun fetchCurrenciesWithGateways(includeGateways: List<GatewayType>?): CurrenciesDto? {
        var currencies = currencyServiceManager.fetchCurrencies()?.currencies
        var gateways = gatewayService.fetchGateways(includeGateways = includeGateways)?.toList()
        var groupedByGateways = gateways?.groupBy { it.currencySymbol }
        return CurrenciesDto(currencies?.stream()?.map {
            it.apply {
                it.gateways = groupedByGateways?.get(it.symbol)
                it.depositAllowed = groupedByGateways?.get(it.symbol)?.stream()?.filter { g -> g.isActive == true }?.map(CurrencyGatewayCommand::depositAllowed)?.reduce { t, u -> t ?: false || u ?: false }?.orElseGet { false }
                it.withdrawAllowed = groupedByGateways?.get(it.symbol)?.stream()?.filter { g -> g.isActive == true }?.map(CurrencyGatewayCommand::withdrawAllowed)?.reduce { t, u -> t ?: false || u ?: false }?.orElseGet { false }
                gateways?.forEach { gateway ->
                        when (gateway) {
                            is OnChainGatewayCommand -> it.availableGatewayType = GatewayType.OnChain.name;
                            is OffChainGatewayCommand -> it.availableGatewayType = GatewayType.OffChain.name;
                        }

                }

            }
            it.toDto()
        }?.collect(Collectors.toList()))
    }


    suspend fun updateGateway(request: CurrencyGatewayCommand): CurrencyGatewayCommand? {
        currencyServiceManager.fetchCurrency(FetchCurrency(symbol = request.currencySymbol))
                ?.let {
//                    if (it.isCryptoCurrency == true)
                    return gatewayService.updateCryptoGateway(request);
                } ?: throw OpexError.CurrencyNotFound.exception()
    }


}