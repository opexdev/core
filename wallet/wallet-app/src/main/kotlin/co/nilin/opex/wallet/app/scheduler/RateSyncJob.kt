package co.nilin.opex.wallet.app.scheduler

import co.nilin.opex.wallet.core.service.otc.RateSyncMonitor
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class RateSyncJob(
    private val rateSyncMonitor: RateSyncMonitor
) {

    private val logger = LoggerFactory.getLogger(RateSyncJob::class.java)

    @Scheduled(fixedDelayString = "\${app.rate-monitor.freshness-check-interval:60000}")
    fun checkFreshness() {
        runBlocking {
            try {
                rateSyncMonitor.checkFreshness()
            } catch (e: Exception) {
                logger.error("Rate freshness check failed: ${e.message}", e)
            }
        }
    }
}
