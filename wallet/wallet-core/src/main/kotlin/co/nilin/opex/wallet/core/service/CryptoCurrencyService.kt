package co.nilin.opex.wallet.core.service

import co.nilin.opex.wallet.core.inout.OnChainGatewayCommand
import co.nilin.opex.wallet.core.inout.CurrencyGatewayCommand
import co.nilin.opex.wallet.core.inout.CurrencyGateways
import co.nilin.opex.wallet.core.spi.BcGatewayProxy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CryptoCurrencyService(private val bcGatewayProxy: BcGatewayProxy,
                            private val authService: AuthService) {
    private val logger = LoggerFactory.getLogger(CryptoCurrencyService::class.java)

    suspend fun createGateway(currencyImp: CurrencyGatewayCommand): CurrencyGatewayCommand? {
        val token = authService.extractToken()
        return bcGatewayProxy.createGateway(currencyImp, token)
    }


    suspend fun updateCryptoGateway(currencyImp: CurrencyGatewayCommand): CurrencyGatewayCommand? {
        val token = authService.extractToken()
        return bcGatewayProxy.updateGateway(currencyImp, token)

    }

    suspend fun fetchGateways(currencySymbol:String?=null): CurrencyGateways? {
        val token = authService.extractToken()
        return bcGatewayProxy.fetchGateways(currencySymbol, token)
    }

    suspend fun fetchGateway(currencyGatewayUuid:String,currencySymbol: String): CurrencyGatewayCommand? {
        val token = authService.extractToken()
        return bcGatewayProxy.fetchGatewayDetail(currencyGatewayUuid,currencySymbol, token)
    }


    suspend fun deleteGateway(currencyGatewayUuid:String, currencySymbol: String) {
        val token = authService.extractToken()
        return bcGatewayProxy.deleteGateway(currencyGatewayUuid,currencySymbol, token)
    }
}


