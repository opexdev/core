package co.nilin.opex.price.core.service

import co.nilin.opex.price.core.spi.Notifier
import co.nilin.opex.price.core.spi.PriceConfigLoader
import co.nilin.opex.price.core.spi.RateHistoryLoader
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

class PriceSyncMonitor(
    private val priceConfigLoader: PriceConfigLoader,
    private val rateHistoryLoader: RateHistoryLoader,
    private val notifier: Notifier,
    private val maxAge: Duration,
) {

    private val logger = LoggerFactory.getLogger(PriceSyncMonitor::class.java)
    private val staleSymbols = ConcurrentHashMap.newKeySet<String>()

    suspend fun checkFreshness() {
        val autoSymbols = priceConfigLoader.loadActiveAutoConfigs().map { it.symbol }
        if (autoSymbols.isEmpty()) return
        staleSymbols.retainAll(autoSymbols.toSet())
        val lastUpdate = rateHistoryLoader.loadLatestForAllSymbols().associate { it.symbol to it.createdDate }
        val now = LocalDateTime.now()

        autoSymbols.forEach { symbol ->
            val age = lastUpdate[symbol]?.let { Duration.between(it, now) }

            if (age == null || age > maxAge) {
                if (staleSymbols.add(symbol)) {
                    logger.warn("Automatic price for {} is stale (age={})", symbol, age)
                    notifier.notify(staleMessage(symbol, age))
                }
            } else if (staleSymbols.remove(symbol)) {
                notifier.notify("✅ Price alert cleared\nAutomatic price for $symbol is being updated again.")
            }
        }
    }

    private fun staleMessage(symbol: String, age: Duration?) = buildString {
        appendLine("⚠️ Stale automatic price")
        appendLine("Symbol: $symbol")
        append(
            if (age == null) "No price has ever been recorded for this symbol."
            else "Last update: ${age.toMinutes()} min ago (threshold: ${maxAge.toMinutes()} min)."
        )
    }
}
