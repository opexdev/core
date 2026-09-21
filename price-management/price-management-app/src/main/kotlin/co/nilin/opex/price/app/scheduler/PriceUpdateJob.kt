package co.nilin.opex.price.app.scheduler

import co.nilin.opex.price.core.service.PriceAggregationJobManager
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class PriceUpdateJob(
    private val priceAggregationJobManager: PriceAggregationJobManager
) {

    private val logger = LoggerFactory.getLogger(PriceUpdateJob::class.java)

    @Scheduled(
        fixedDelayString = "\${app.price-management.update-interval:10000}",
    )
    fun updatePrices() {
        runBlocking {
            try {
                priceAggregationJobManager.updatePrices()
            } catch (e: Exception) {
                logger.error("Price update job failed: ${e.message}", e)
            }
        }
    }
}
