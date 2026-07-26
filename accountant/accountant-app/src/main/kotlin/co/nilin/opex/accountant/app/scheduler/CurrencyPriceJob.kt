package co.nilin.opex.accountant.app.scheduler

import co.nilin.opex.accountant.core.spi.CurrencyRatePersister
import co.nilin.opex.accountant.core.spi.WalletProxy
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.math.BigDecimal


@Service
class CurrencyPriceJob(
    private val currencyRatePersister: CurrencyRatePersister,
    private val walletProxy: WalletProxy,
    @Value("\${app.trade-volume-calculation-currency}")
    private val tradeVolumeCalculationCurrency: String,
    @Value("\${app.withdraw-volume-calculation-currency}")
    private val withdrawVolumeCalculationCurrency: String
) {

    @Scheduled(fixedDelay = 120_000, initialDelay = 120_000)
    fun updatePrices() {
        runBlocking {
            updateCurrencyRates(tradeVolumeCalculationCurrency)

            if (tradeVolumeCalculationCurrency != withdrawVolumeCalculationCurrency) {
                updateCurrencyRates(withdrawVolumeCalculationCurrency)
            }
        }
    }
    private suspend fun updateCurrencyRates(quoteCurrency: String) {
        walletProxy.getPrices(quoteCurrency)
            .forEach { price ->
                currencyRatePersister.updateRate(
                    price.currency,
                    quoteCurrency,
                    price.sellPrice ?: BigDecimal.ZERO
                )
            }
    }
}
