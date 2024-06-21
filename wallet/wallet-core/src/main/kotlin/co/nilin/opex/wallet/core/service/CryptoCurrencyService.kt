package co.nilin.opex.wallet.core.service

import co.nilin.opex.wallet.core.inout.CryptoCurrencyCommand
import co.nilin.opex.wallet.core.inout.CryptoImps
import co.nilin.opex.wallet.core.spi.BcGatewayProxy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CryptoCurrencyService(private val bcGatewayProxy: BcGatewayProxy,
                            private val authService: AuthService) {
    private val logger = LoggerFactory.getLogger(CryptoCurrencyService::class.java)

    suspend fun createImpl(currencyImp: CryptoCurrencyCommand): CryptoCurrencyCommand? {
        val token = authService.extractToken()
        return bcGatewayProxy.createImpl(currencyImp, token)
    }


    suspend fun updateCryptoImpl(currencyImp: CryptoCurrencyCommand): CryptoCurrencyCommand? {
        val token = authService.extractToken()
        return bcGatewayProxy.updateImpl(currencyImp, token)

    }

    suspend fun fetchImpls(currencySymbol:String?=null): CryptoImps? {
        val token = authService.extractToken()
        return bcGatewayProxy.fetchImpls(currencySymbol, token)
    }

    suspend fun fetchImpl(currencyImplUuid:String,currencySymbol: String): CryptoCurrencyCommand? {
        val token = authService.extractToken()
        return bcGatewayProxy.fetchImplDetail(currencyImplUuid,currencySymbol, token)
    }


    suspend fun deleteImpl(currencyImplUuid:String, currencySymbol: String) {
        val token = authService.extractToken()
        return bcGatewayProxy.deleteImpl(currencyImplUuid,currencySymbol, token)
    }
}


