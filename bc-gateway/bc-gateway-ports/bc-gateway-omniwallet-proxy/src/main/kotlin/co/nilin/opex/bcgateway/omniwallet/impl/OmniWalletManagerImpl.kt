package co.nilin.opex.bcgateway.omniwallet.impl

import co.nilin.opex.bcgateway.core.model.CryptoCurrencyCommand
import co.nilin.opex.bcgateway.core.model.OmniBalance
import co.nilin.opex.bcgateway.core.spi.OmniWalletManager
import co.nilin.opex.bcgateway.omniwallet.model.AddressBalanceWithUsd
import co.nilin.opex.bcgateway.omniwallet.model.ChainBalanceResponse
import co.nilin.opex.bcgateway.omniwallet.proxy.OmniWalletProxy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class OmniWalletManagerImpl(private val omniWalletProxy: OmniWalletProxy) : OmniWalletManager {
    private val logger = LoggerFactory.getLogger(omniWalletProxy::class.java)

    override suspend fun getTokenBalance(cryptoCurrencyCommand: CryptoCurrencyCommand): OmniBalance {
        return OmniBalance(currency = cryptoCurrencyCommand.currencySymbol,
                network = cryptoCurrencyCommand.chain,
                balance = omniWalletProxy.getTokenBalance(cryptoCurrencyCommand.tokenAddress!!, cryptoCurrencyCommand.chain)?.stream()?.map(AddressBalanceWithUsd::balance)?.reduce { a, b -> a + b }?.orElse(BigDecimal.ZERO))
    }

    override suspend fun getAssetBalance(cryptoCurrencyCommand: CryptoCurrencyCommand): OmniBalance {
        return OmniBalance(cryptoCurrencyCommand.currencySymbol, cryptoCurrencyCommand.chain, omniWalletProxy.getAssetBalance(cryptoCurrencyCommand.chain)?.balance?: BigDecimal.ZERO)
    }
}