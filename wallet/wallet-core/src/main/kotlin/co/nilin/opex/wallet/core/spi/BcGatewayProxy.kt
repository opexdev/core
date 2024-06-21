package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.CryptoCurrencyCommand
import co.nilin.opex.wallet.core.inout.CryptoImps

interface BcGatewayProxy {
    suspend fun createImpl(currencyImp: CryptoCurrencyCommand, internalToken: String?): CryptoCurrencyCommand?

    suspend fun updateImpl(currencyImp: CryptoCurrencyCommand, internalToken: String?): CryptoCurrencyCommand?

    suspend fun fetchImpls(symbol: String?, internalToken: String?): CryptoImps?

    suspend fun fetchImplDetail(implUuid: String, currencySymbol: String, internalToken: String?): CryptoCurrencyCommand?

    suspend fun deleteImpl(implUuid: String, currencySymbol: String, internalToken: String?)

}