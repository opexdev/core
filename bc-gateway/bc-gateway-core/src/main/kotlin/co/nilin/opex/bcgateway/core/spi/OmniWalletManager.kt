package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.CryptoCurrencyCommand
import co.nilin.opex.bcgateway.core.model.CurrencyImplementation
import co.nilin.opex.bcgateway.core.model.OmniBalance

interface OmniWalletManager {

    suspend fun getTokenBalance(currencyImpl:CryptoCurrencyCommand):OmniBalance
    suspend fun getAssetBalance(cryptoCurrencyCommand: CryptoCurrencyCommand):OmniBalance

}