package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.api.FinancialActionProcessor
import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.FinancialActionStatus
import co.nilin.opex.accountant.core.spi.FinancialActionLoader
import co.nilin.opex.accountant.core.spi.FinancialActionPersister
import co.nilin.opex.accountant.core.spi.FinancialActionPublisher
import org.slf4j.LoggerFactory

class FinancialActionProcessorImpl(
    private val financialActionLoader: FinancialActionLoader,
    private val financialActionPersister: FinancialActionPersister,
    private val financialActionPublisher: FinancialActionPublisher
) : FinancialActionProcessor {

    private val logger = LoggerFactory.getLogger(FinancialActionProcessor::class.java)

    override suspend fun process(financialAction: FinancialAction) {
        val list = ArrayList<FinancialAction>()
        extractParents(financialAction, list)
        process(list)
    }

    override suspend fun process(financialActions: List<FinancialAction>) {
        for (i in financialActions.indices) {
            val fa = financialActions[i]
            try {
                if (fa.status == FinancialActionStatus.CREATED)
                    submitEvent(fa)
            } catch (e: Exception) {
                logger.error("Unable to submit financial action $fa", e)
                break
            }
            financialActionPersister.updateStatus(fa.uuid, FinancialActionStatus.PROCESSED)
        }
    }

    override suspend fun batchProcess(offset: Long, size: Long) {
        val factions = financialActionLoader.loadUnprocessed(offset, size)
        val list = ArrayList<FinancialAction>()
        factions.forEach { extractParents(it, list) }
        process(list)
    }

    private suspend fun submitEvent(financialAction: FinancialAction) {
        financialActionPublisher.publish(financialAction)
    }

    fun extractParents(financialAction: FinancialAction, list: ArrayList<FinancialAction>) {
        if (financialAction.parent != null) {
            extractParents(financialAction.parent, list)
        }

        if (!list.contains(financialAction))
            list.add(financialAction)
    }

}