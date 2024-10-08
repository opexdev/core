package co.nilin.opex.wallet.core.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.spi.GatewayPersister
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.util.*
import kotlin.collections.ArrayList

@Service
class GatewayService(@Qualifier("onChainGateway") private val onChainGateway: GatewayPersister,
                     @Qualifier("offChainGateway") private val offChainGateway: GatewayPersister,
                     @Qualifier("manualGateway") private val manualGateway: GatewayPersister,
                     private val authService: AuthService) {
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

            is ManualGatewayCommand -> {
                currencyGateway.apply { gatewayUuid = "mag_$gatewayUuid" }
                return manualGateway.createGateway(currencyGateway)
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

            is ManualGatewayCommand -> {
                return manualGateway.updateGateway(currencyGateway)
            }

            else -> throw OpexError.BadRequest.exception()

        }
    }

    suspend fun fetchGateways(currencySymbol: String? = null, includeGateways: List<GatewayType>? = null): List<CurrencyGatewayCommand>? {
        includeGateways?.map { logger.info(it.name) }
        var gateways = ArrayList<CurrencyGatewayCommand>()
        if (includeGateways != null) {
            if (GatewayType.Manually in includeGateways)
                manualGateway.fetchGateways(currencySymbol)?.toList()?.let { it1 -> gateways.addAll(it1) }

            if (GatewayType.OffChain in includeGateways)
                offChainGateway.fetchGateways(currencySymbol)?.toList()?.let { it1 -> gateways.addAll(it1) }

            if (GatewayType.OnChain in includeGateways) {
                val token = authService.extractToken()
                onChainGateway.fetchGateways(currencySymbol, token)?.toList()?.let { it1 -> gateways.addAll(it1) }
            }

        }
        return gateways
    }


    suspend fun fetchGateway(currencyGatewayUuid: String, currencySymbol: String): CurrencyGatewayCommand? {
        val token = authService.extractToken()
        return onChainGateway.fetchGatewayDetail(currencyGatewayUuid, currencySymbol, token)
    }


    suspend fun deleteGateway(currencyGatewayUuid: String, currencySymbol: String) {
        if (currencyGatewayUuid.startsWith("ong")) {
            val token = authService.extractToken()
            return onChainGateway.deleteGateway(currencyGatewayUuid, currencySymbol, token)
        }

        if (currencyGatewayUuid.startsWith("ofg")) {
            offChainGateway.deleteGateway(currencyGatewayUuid,
                    currencySymbol)
        }

        if (currencyGatewayUuid.startsWith("mag")) {
            manualGateway.deleteGateway(currencyGatewayUuid,
                    currencySymbol)
        }


    }
}


