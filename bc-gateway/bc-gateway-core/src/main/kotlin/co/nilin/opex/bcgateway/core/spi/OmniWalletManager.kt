package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.OmniBalance

interface OmniWalletManager {

    suspend fun getTokenBalance(tokenAddress:String, network:String):OmniBalance
    suspend fun getAssetBalance(currency:String,network:String):OmniBalance

}