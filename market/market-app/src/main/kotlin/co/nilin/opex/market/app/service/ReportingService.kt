package co.nilin.opex.market.app.service

import co.nilin.opex.market.app.utils.asLocalDateTime
import co.nilin.opex.market.core.spi.MarketQueryHandler
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ReportingService(private val marketQueryHandler: MarketQueryHandler) {
    private val logger = LoggerFactory.getLogger(ReportingService::class.java)

    @Scheduled(initialDelay = 60000, fixedDelay = 1000 * 60 * 30)
    private fun reporting() {
        runBlocking{
            try {

                val count = marketQueryHandler.numberOfOrders(LocalDateTime.now().minusMinutes(15), null)
                logger.info("in the last 30 minutes, the number of orders : $count")
            } catch (e: Exception) {
                logger.error("Could not report orders cont", e)
            }
        }
    }
}