package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.CryptoCurrencyCommand
import co.nilin.opex.wallet.core.inout.CryptoImps

interface BcGatewayProxy {
    suspend fun createNewCurrency(currencyImp: CryptoCurrencyCommand, internalToken: String?): CryptoImps?

    suspend fun updateImpOfCryptoCurrency(currencyImp: CryptoCurrencyCommand, internalToken: String?): CryptoImps?

    suspend fun fetchImpsOfCryptoCurrency(symbol: String, internalToken: String? ): CryptoImps?

}