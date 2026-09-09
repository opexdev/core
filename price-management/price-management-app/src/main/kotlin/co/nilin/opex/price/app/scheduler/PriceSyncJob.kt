package co.nilin.opex.price.app.scheduler

import co.nilin.opex.price.core.service.PriceSyncMonitor
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class PriceSyncJob(
    private val priceSyncMonitor: PriceSyncMonitor
) {

    private val logger = LoggerFactory.getLogger(PriceSyncJob::class.java)

    @Scheduled(fixedDelayString = "\${app.price-management.freshness-check-interval:60000}")
    fun checkFreshness() {
        runBlocking {
            try {
                priceSyncMonitor.checkFreshness()
            } catch (e: Exception) {
                logger.error("Price freshness check failed: ${e.message}", e)
            }
        }
    }
}
