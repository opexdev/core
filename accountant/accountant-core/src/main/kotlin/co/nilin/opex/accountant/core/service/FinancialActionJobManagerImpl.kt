package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.api.FinancialActionJobManager
import co.nilin.opex.accountant.core.inout.TransferRequest
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

    override suspend fun processFinancialActions(offset: Long, size: Long) {
        val factions = financialActionLoader.loadUnprocessed(offset, size)
        val flatten = sortAndFlattenFA(factions)
        logger.info("Loaded ${flatten.size} factions: ${flatten.map { it.id }}")
        if (factions.isEmpty())
            return

        try {
            val requests = factions.map {
                TransferRequest(
                    it.amount,
                    it.symbol,
                    it.sender,
                    it.senderWalletType,
                    it.receiver,
                    it.receiverWalletType,
                    null,
                    it.eventType + it.pointer
                )
            }
            walletProxy.batchTransfer(requests)
            financialActionPersister.updateBatchStatus(factions, FinancialActionStatus.PROCESSED)
        } catch (e: Exception) {
            logger.error("financial job error", e)
        }
    }

    fun sortAndFlattenFA(list: List<FinancialAction>): Collection<FinancialAction> {
        val result = arrayListOf<FinancialAction>()

        fun extractParent(fa: FinancialAction) {
            if (fa.parent != null)
                extractParent(fa.parent)
            result.add(fa)
        }
        list.forEach { extractParent(it) }
        return result.distinctBy { it.id }
    }
}