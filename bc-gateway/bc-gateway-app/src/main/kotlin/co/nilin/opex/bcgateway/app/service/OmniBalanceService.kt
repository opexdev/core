package co.nilin.opex.bcgateway.app.service

import co.nilin.opex.bcgateway.core.model.FetchImpls
import co.nilin.opex.bcgateway.core.spi.CryptoCurrencyHandler
import co.nilin.opex.bcgateway.core.spi.CryptoCurrencyHandlerV2
import co.nilin.opex.bcgateway.core.spi.OmniWalletManager
import co.nilin.opex.common.OpexError
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class OmniBalanceService(private val cryptoCurrencyHandlerV2: CryptoCurrencyHandlerV2,
                         private val omniWalletManager: OmniWalletManager) {

    data class OmniBalanceForCurrency(val currency: String, val balance: BigDecimal? = BigDecimal.ZERO)
    data class OmniBalance(val data: ArrayList<OmniBalanceForCurrency>? = ArrayList())
    private val logger = LoggerFactory.getLogger(OmniBalanceService::class.java)

    suspend fun fetchSystemBalance(currency: String): OmniBalanceForCurrency {
        val currencyImpls = cryptoCurrencyHandlerV2.fetchCurrencyImpls(FetchImpls(currencySymbol = currency))?.imps
                ?: throw OpexError.CurrencyNotFound.exception()

        val totalBalance: BigDecimal = currencyImpls?.map {
            when (it.isToken) {
                true -> it.tokenAddress?.let { ta -> omniWalletManager.getTokenBalance(it).balance }
                        ?: BigDecimal.ZERO

                false -> omniWalletManager.getAssetBalance(it).balance ?: BigDecimal.ZERO
                else -> BigDecimal.ZERO
            }
        }.reduce { a, b -> a + b }


        return OmniBalanceForCurrency(currency = currency, balance = totalBalance)
    }

    suspend fun fetchSystemBalance(): List<OmniBalanceForCurrency>? {
        logger.info("going to fetch balance .......")
        val currencyImpls = cryptoCurrencyHandlerV2.fetchCurrencyImpls(FetchImpls())?.imps
                ?: throw OpexError.CurrencyNotFound.exception()
        val implsGroupedByCurrency = currencyImpls.groupBy { it.currencySymbol }
        val result = ArrayList<OmniBalanceForCurrency>()
        for (currency in implsGroupedByCurrency.keys) {
            val balance = implsGroupedByCurrency[currency]?.map {
                when (it.isToken) {
                    true -> it.tokenAddress?.let { ta -> omniWalletManager.getTokenBalance(it).balance }
                            ?: BigDecimal.ZERO

                    false -> omniWalletManager.getAssetBalance(it).balance ?: BigDecimal.ZERO
                    else -> BigDecimal.ZERO
                }
            }?.reduce { a, b -> a + b }
            result.add(OmniBalanceForCurrency(currency, balance))
        }
        return result.toList()
    }

}