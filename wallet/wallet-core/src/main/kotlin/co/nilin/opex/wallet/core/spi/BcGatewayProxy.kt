package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.CryptoCurrencyCommand
import co.nilin.opex.wallet.core.inout.CryptoImps

interface BcGatewayProxy {
    suspend fun createNewCurrency(currencyImp: CryptoCurrencyCommand, internalToken: String?): CryptoImps?

    suspend fun updateImplOfCryptoCurrency(currencyImp: CryptoCurrencyCommand, internalToken: String?): CryptoImps?

    suspend fun fetchImpls(symbol: String?, internalToken: String? ): CryptoImps?

    suspend fun fetchImplDetail(implUuid: String, internalToken: String? ): CryptoCurrencyCommand?


}