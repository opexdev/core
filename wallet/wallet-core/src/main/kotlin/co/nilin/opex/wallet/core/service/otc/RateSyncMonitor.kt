package co.nilin.opex.wallet.core.service.otc

import co.nilin.opex.wallet.core.model.otc.StaleRate
import co.nilin.opex.wallet.core.spi.Notifier
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Component
class RateSyncMonitor(
    private val rateService: RateService,
    private val notifier: Notifier,
    @Value("\${app.rate-monitor.stale-alert-age-seconds:1800}") private val staleAlertAgeSeconds: Long,
) {

    private val logger = LoggerFactory.getLogger(RateSyncMonitor::class.java)

    private val stalePairs = ConcurrentHashMap.newKeySet<String>()

    suspend fun checkFreshness() {
        val maxAge = Duration.ofSeconds(staleAlertAgeSeconds)
        val now = LocalDateTime.now()
        val stale = rateService.findStaleRates(now.minus(maxAge))
        val stalePairKeys = stale.map { it.pairKey() }.toSet()

        stalePairs.filterNot { it in stalePairKeys }.forEach { pair ->
            if (notifier.notify("✅ Rate alert cleared\nRate for $pair is being updated again.")) {
                stalePairs.remove(pair)
            }
        }

        stale.forEach { rate ->
            val pair = rate.pairKey()
            if (pair in stalePairs) return@forEach

            val age = Duration.between(rate.lastUpdateDate, now)
            logger.warn("Rate {} is stale (age={})", pair, age)

            if (notifier.notify(staleMessage(rate, age, maxAge))) {
                stalePairs.add(pair)
            }
        }
    }

    private fun StaleRate.pairKey() = "$sourceSymbol/$destSymbol"

    private fun staleMessage(rate: StaleRate, age: Duration, maxAge: Duration) = buildString {
        appendLine("⚠️ Stale wallet rate")
        appendLine("Pair: ${rate.pairKey()}")
        append("Last update: ${age.toMinutes()} min ago (threshold: ${maxAge.toMinutes()} min).")
    }
}
