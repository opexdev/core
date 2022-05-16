package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.api.FinancialActionJobManager
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

    private val retryLimit = 10
    private val log = LoggerFactory.getLogger(FinancialActionJobManagerImpl::class.java)

    override suspend fun processFinancialActions(offset: Long, size: Long) {
        val factions = financialActionLoader.loadUnprocessed(offset, size)
        factions.forEach {
            try {
                walletProxy.transfer(
                    it.symbol,
                    it.senderWalletType,
                    it.sender,
                    it.receiverWalletType,
                    it.receiver,
                    it.amount,
                    it.eventType + it.pointer,
                    "fa${it.id}"
                )
                financialActionPersister.updateStatus(it, FinancialActionStatus.PROCESSED)
            } catch (e: Exception) {
                log.error("financial job error", e)
                financialActionPersister.updateStatus(
                    it,
                    if (it.retryCount >= retryLimit) FinancialActionStatus.ERROR else FinancialActionStatus.CREATED
                )
            }
        }
    }
}