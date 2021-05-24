package co.nilin.mixchange.accountant.core.service

import co.nilin.mixchange.accountant.core.api.FinancialActionJobManager
import co.nilin.mixchange.accountant.core.model.FinancialActionStatus
import co.nilin.mixchange.accountant.core.spi.FinancialActionPersister
import co.nilin.mixchange.accountant.core.spi.FinancialActionLoader
import co.nilin.mixchange.accountant.core.spi.WalletProxy

class FinancialActionJobManagerImpl(val financialActionLoader: FinancialActionLoader
                                    , val financialActionPersister: FinancialActionPersister
                                    , val walletProxy: WalletProxy): FinancialActionJobManager {
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
            } catch (e: Exception){
                financialActionPersister.updateStatus(faction, FinancialActionStatus.ERROR)
            }
        }
    }
}