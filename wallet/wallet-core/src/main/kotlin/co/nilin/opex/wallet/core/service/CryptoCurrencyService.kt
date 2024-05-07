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


    suspend fun updateCryptoImp(currencyImp: CryptoCurrencyCommand): CryptoImps? {
        val token = authService.extractToken()
        return bcGatewayProxy.updateImpOfCryptoCurrency(currencyImp, token)
    }

    suspend fun fetchCurrencyImps(currencyUuid:String): CryptoImps? {
        val token = authService.extractToken()
        return bcGatewayProxy.fetchImpsOfCryptoCurrency(currencyUuid, token)
    }



}


