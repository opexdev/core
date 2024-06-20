package co.nilin.opex.wallet.core.service

import co.nilin.opex.wallet.core.inout.CryptoCurrencyCommand
import co.nilin.opex.wallet.core.inout.CryptoImps
import co.nilin.opex.wallet.core.spi.BcGatewayProxy
import org.springframework.stereotype.Service

@Service
class CryptoCurrencyService(private val bcGatewayProxy: BcGatewayProxy,
                            private val authService: AuthService) {
    suspend fun toCryptoCurrency(currencyImp: CryptoCurrencyCommand): CryptoImps? {
        val token = authService.extractToken()
        return bcGatewayProxy.createNewCurrency(currencyImp, token)
    }


    suspend fun updateCryptoImpl(currencyImp: CryptoCurrencyCommand): CryptoImps? {
        val token = authService.extractToken()
        return bcGatewayProxy.updateImplOfCryptoCurrency(currencyImp, token)
    }

    suspend fun fetchImpls(currencySymbol:String?): CryptoImps? {
        val token = authService.extractToken()
        return bcGatewayProxy.fetchImpls(currencySymbol, token)
    }

    suspend fun fetchImpl(currencyImplUuid:String): CryptoCurrencyCommand? {
        val token = authService.extractToken()
        return bcGatewayProxy.fetchImplDetail(currencyImplUuid, token)
    }

}


