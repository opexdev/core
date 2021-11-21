package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.api.FinancialActionJobManager
import co.nilin.opex.accountant.core.model.FinancialActionStatus
import co.nilin.opex.accountant.core.spi.FinancialActionLoader
import co.nilin.opex.accountant.core.spi.FinancialActionPersister
import co.nilin.opex.accountant.core.spi.WalletProxy
import org.slf4j.LoggerFactory

class FinancialActionJobManagerImpl(
    val financialActionLoader: FinancialActionLoader,
    val financialActionPersister: FinancialActionPersister,
    val walletProxy: WalletProxy
) : FinancialActionJobManager {

    private val log = LoggerFactory.getLogger(FinancialActionJobManagerImpl::class.java)

    override suspend fun processFinancialActions(offset: Long, size: Long) {
        val factions = financialActionLoader.loadUnprocessed(offset, size)
        factions.forEach { faction ->
            try {
                walletProxy.transfer(
                    faction.symbol,
                    faction.senderWalletType,
                    faction.sender,
                    faction.receiverWalletType,
                    faction.receiver,
                    faction.amount,
                    faction.eventType + faction.pointer,
                    null
                )
                financialActionPersister.updateStatus(faction, FinancialActionStatus.PROCESSED)
            } catch (e: Exception) {
                log.error("financial job error", e)
                financialActionPersister.updateStatus(faction, FinancialActionStatus.ERROR)
            }
        }
    }
}