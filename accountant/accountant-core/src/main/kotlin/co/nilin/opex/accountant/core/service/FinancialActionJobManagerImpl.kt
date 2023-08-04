package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.api.FinancialActionJobManager
import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.FinancialActionStatus
import co.nilin.opex.accountant.core.spi.FinancialActionLoader
import co.nilin.opex.accountant.core.spi.FinancialActionPersister
import co.nilin.opex.accountant.core.spi.WalletProxy
import org.slf4j.LoggerFactory

class FinancialActionJobManagerImpl(
    private val financialActionLoader: FinancialActionLoader,
    private val financialActionPersister: FinancialActionPersister,
    private val walletProxy: WalletProxy
) : FinancialActionJobManager {

    private val logger = LoggerFactory.getLogger(FinancialActionJobManagerImpl::class.java)

    override suspend fun processFinancialActions(offset: Long, size: Long){
        val factions = financialActionLoader.loadReadyToProcess(offset, size)
        factions.forEach {
            try {
                if (it.parent != null) {
                    val reloadParent = financialActionLoader.loadFinancialAction(it.parent.id)!!
                    if (reloadParent.status != FinancialActionStatus.PROCESSED) {
                        logger.warn("financial job {} skipped because of parent status {}", it, reloadParent)
                        return@forEach
                    }
                }
                walletProxy.transfer(
                    it.symbol,
                    it.senderWalletType,
                    it.sender,
                    it.receiverWalletType,
                    it.receiver,
                    it.amount,
                    it.eventType + it.pointer,
                    it.id.toString(),
                    it.category.toString(),
                    it.detail
                )
                financialActionPersister.updateStatusNewTx(it, FinancialActionStatus.PROCESSED)

            } catch (e: Exception) {
                logger.error("financial job error", e)
                financialActionPersister.updateStatusNewTx(it, FinancialActionStatus.ERROR)
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