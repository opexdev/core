package co.nilin.opex.wallet.core.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.spi.GatewayPersister
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.util.*

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

    suspend fun fetchGateways(currencySymbol: String? = null): CurrencyGateways? {
        val token = authService.extractToken()
        return onChainGateway.fetchGateways(currencySymbol, token)


    }

    suspend fun fetchGateway(currencyGatewayUuid: String, currencySymbol: String): CurrencyGatewayCommand? {

        val token = authService.extractToken()
        return onChainGateway.fetchGatewayDetail(currencyGatewayUuid, currencySymbol, token)
    }


    suspend fun deleteGateway(currencyGatewayUuid: String, currencySymbol: String) {
//        when (currencyGateway.type) {
//            GatewayType.OnChain -> {
//                val token = authService.extractToken()
//                return onChainGateway.deleteGateway(currencyGatewayUuid, currencySymbol, token)
//            }
//
//            GatewayType.OffChain -> {
//
//                return null
//            }
//
//            GatewayType.Manually -> {
//                return null
//
//            }
//        }

    }
}


