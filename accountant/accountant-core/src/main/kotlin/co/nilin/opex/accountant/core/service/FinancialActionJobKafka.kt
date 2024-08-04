package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.spi.FinancialActionLoader
import co.nilin.opex.accountant.core.spi.FinancialActionPersister
import co.nilin.opex.accountant.core.spi.FinancialActionPublisher
import co.nilin.opex.common.utils.LoggerDelegate
import co.nilin.opex.utility.error.data.OpexException
import org.springframework.stereotype.Component

@Component
class FinancialActionJobKafka(
    private val loader: FinancialActionLoader,
    private val persister: FinancialActionPersister,
    private val publisher: FinancialActionPublisher
) {

    private val logger by LoggerDelegate()

    suspend fun processFinancialActions() {
        val actions = loader.loadReadyToProcess(0, 100)
        if (actions.isNotEmpty()) logger.info("Processing ${actions.size} financial actions")

        for (fi in actions) {
            try {
                if (fi.parent != null && !fi.parent.isProcessed()) {
                    logger.warn("Financial job (uuid=${fi.uuid}) skipped because of parent status: uuid=${fi.parent.uuid}, status=${fi.parent.status}")
                    continue
                }

                publisher.publish(fi)
            } catch (e: Exception) {
                logger.error("Retry financial job error for ${fi.uuid}: ${e.message}")
                persister.updateWithError(
                    fi,
                    if (e is OpexException) e.error.errorName() ?: "" else e.javaClass.name,
                    e.message
                )
            }
        }
    }

    suspend fun retryFinancialActions() {

    }
}