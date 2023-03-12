package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.api.FinancialActionJobManager
import co.nilin.opex.accountant.core.inout.TransferRequest
import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.FinancialActionStatus
import co.nilin.opex.accountant.core.spi.FinancialActionLoader
import co.nilin.opex.accountant.core.spi.FinancialActionPersister
import co.nilin.opex.accountant.core.spi.FinancialActionPublisher
import co.nilin.opex.accountant.core.spi.WalletProxy
import org.slf4j.LoggerFactory

class FinancialActionJobManagerImpl(
    private val financialActionLoader: FinancialActionLoader,
    private val financialActionPersister: FinancialActionPersister,
    private val financialActionPublisher: FinancialActionPublisher,
) : FinancialActionJobManager {

    private val logger = LoggerFactory.getLogger(FinancialActionJobManagerImpl::class.java)

    override suspend fun processFinancialActions(offset: Long, size: Long) {
        val factions = financialActionLoader.loadUnprocessed(offset, size)
        publishFinancialActions(factions)
    }

    private suspend fun publishFinancialActions(financialActions: List<FinancialAction>) {
        val list = arrayListOf<FinancialAction>()
        financialActions.forEach { extractFAParents(it, list) }
        for (fa in list) {
            if (fa.status == FinancialActionStatus.CREATED) {
                try {
                    financialActionPublisher.publish(fa)
                } catch (e: Exception) {
                    logger.error("Cannot publish fa ${fa.uuid}", e)
                    break
                }
                financialActionPersister.updateStatus(fa, FinancialActionStatus.PROCESSED)
            }
        }
    }

    private fun extractFAParents(financialAction: FinancialAction, list: ArrayList<FinancialAction>) {
        if (financialAction.parent != null) {
            extractFAParents(financialAction.parent, list)
        }

        if (!list.contains(financialAction))
            list.add(financialAction)
    }
}