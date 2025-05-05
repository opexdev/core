package co.nilin.opex.wallet.core.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.CurrencyGatewayCommand
import co.nilin.opex.wallet.core.inout.GatewayType
import co.nilin.opex.wallet.core.inout.OffChainGatewayCommand
import co.nilin.opex.wallet.core.inout.OnChainGatewayCommand
import co.nilin.opex.wallet.core.spi.GatewayPersister
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class GatewayService(
    @Qualifier("onChainGateway") private val onChainGateway: GatewayPersister,
    @Qualifier("offChainGateway") private val offChainGateway: GatewayPersister,
    private val authService: AuthService,
) {
    private val logger = LoggerFactory.getLogger(GatewayService::class.java)
    suspend fun createGateway(currencyGateway: CurrencyGatewayCommand): CurrencyGatewayCommand? {
        when (currencyGateway) {
            is OnChainGatewayCommand -> {
                currencyGateway.apply { gatewayUuid = "ong_$gatewayUuid" }
                val token = authService.extractToken()
                return onChainGateway.createGateway(currencyGateway, token)
            }

            is OffChainGatewayCommand -> {
                currencyGateway.apply { gatewayUuid = "ofg_$gatewayUuid" }
                return offChainGateway.createGateway(currencyGateway)
            }

            else -> throw OpexError.BadRequest.exception()
        }
    }

    suspend fun updateCryptoGateway(currencyGateway: CurrencyGatewayCommand): CurrencyGatewayCommand? {
        when (currencyGateway) {
            is OnChainGatewayCommand -> {
                val token = authService.extractToken()
                return onChainGateway.updateGateway(currencyGateway, token)
            }

            is OffChainGatewayCommand -> {
                return offChainGateway.updateGateway(currencyGateway)
            }

            else -> throw OpexError.BadRequest.exception()

        }
    }

    suspend fun fetchGateways(
        currencySymbol: String? = null,
        includeGateways: List<GatewayType>? = null,
    ): List<CurrencyGatewayCommand>? {
        includeGateways?.map { logger.info(it.name) }
        var gateways = ArrayList<CurrencyGatewayCommand>()
        if (includeGateways != null) {
            if (GatewayType.OffChain in includeGateways) offChainGateway.fetchGateways(currencySymbol)?.toList()
                ?.let { it1 -> gateways.addAll(it1) }

            if (GatewayType.OnChain in includeGateways) {
                val token = authService.extractToken()
                onChainGateway.fetchGateways(currencySymbol, token)?.toList()?.let { it1 -> gateways.addAll(it1) }
            }

        }
        return gateways
    }


    suspend fun fetchGateway(currencyGatewayUuid: String, currencySymbol: String): CurrencyGatewayCommand? {
        if (currencyGatewayUuid.startsWith("ofg")) {
            return offChainGateway.fetchGatewayDetail(currencyGatewayUuid, currencySymbol)
        } else if (currencyGatewayUuid.startsWith("ong")) {
            val token = authService.extractToken()
            return onChainGateway.fetchGatewayDetail(currencyGatewayUuid, currencySymbol, token)

        } else throw OpexError.GatewayNotFount.exception()
    }


    suspend fun deleteGateway(currencyGatewayUuid: String, currencySymbol: String) {

        if (currencyGatewayUuid.startsWith("ofg")) {
            offChainGateway.deleteGateway(
                currencyGatewayUuid, currencySymbol
            )
        } else {
            val token = authService.extractToken()
            return onChainGateway.deleteGateway(currencyGatewayUuid, currencySymbol, token)
        }


    }
}


