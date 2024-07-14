package co.nilin.opex.bcgateway.omniwallet.impl

import co.nilin.opex.bcgateway.core.model.OmniBalance
import co.nilin.opex.bcgateway.core.spi.OmniWalletManager
import co.nilin.opex.bcgateway.omniwallet.model.ChainBalanceResponse
import co.nilin.opex.bcgateway.omniwallet.proxy.OmniWalletProxy
import org.springframework.stereotype.Component

@Component
class OmniWalletManagerImpl(private val omniWalletProxy: OmniWalletProxy) : OmniWalletManager {
    override suspend fun getTokenBalance(tokenAddress: String, network: String): OmniBalance {
        return omniWalletProxy.getTokenBalance(tokenAddress, network)
    }

    override suspend fun getAssetBalance(currency:String,network: String): OmniBalance {
        return OmniBalance(currency,network,omniWalletProxy.getAssetBalance( network).data.map { it.balance }.reduce{a,b->a+b})
    }
}