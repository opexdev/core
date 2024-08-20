package co.nilin.opex.wallet.core.service

import co.nilin.opex.wallet.core.inout.CurrencyGatewayCommand
import co.nilin.opex.wallet.core.inout.CurrencyGateways
import co.nilin.opex.wallet.core.inout.GatewayType
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
        when (currencyGateway.type) {
            GatewayType.OnChain -> {
                currencyGateway.apply { gatewayUuid = "onc_$gatewayUuid" }
                val token = authService.extractToken()
                return onChainGateway.createGateway(currencyGateway, token)
            }
            GatewayType.OffChain -> {
                currencyGateway.apply { gatewayUuid = "ofc_$gatewayUuid" }
                return offChainGateway.createGateway(currencyGateway)
            }
            GatewayType.Manually -> {
                currencyGateway.apply { gatewayUuid = "mac_$gatewayUuid" }
                return manualGateway.createGateway(currencyGateway)
            }
        }
    }

    suspend fun updateCryptoGateway(currencyGateway: CurrencyGatewayCommand): CurrencyGatewayCommand? {
        when (currencyGateway.type) {
            GatewayType.OnChain -> {
                val token = authService.extractToken()
                return onChainGateway.updateGateway(currencyGateway, token)
            }
            GatewayType.OffChain -> {
                return offChainGateway.updateGateway(currencyGateway)
            }
            GatewayType.Manually -> {
                return manualGateway.updateGateway(currencyGateway)
            }
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


