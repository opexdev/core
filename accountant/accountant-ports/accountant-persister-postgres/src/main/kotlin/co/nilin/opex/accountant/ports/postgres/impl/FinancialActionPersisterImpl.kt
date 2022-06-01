package co.nilin.opex.accountant.ports.postgres.impl

import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.FinancialActionStatus
import co.nilin.opex.accountant.core.spi.FinancialActionPersister
import co.nilin.opex.accountant.ports.postgres.dao.FinancialActionRepository
import co.nilin.opex.accountant.ports.postgres.model.FinancialActionModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

@Component
class FinancialActionPersisterImpl(private val financialActionRepository: FinancialActionRepository) :
    FinancialActionPersister {

    override suspend fun persist(financialActions: List<FinancialAction>): List<FinancialAction> {
        financialActionRepository.saveAll(financialActions.map {
            FinancialActionModel(
                null,
                it.parent?.id,
                it.eventType,
                it.pointer,
                it.symbol,
                it.amount,
                it.sender,
                it.senderWalletType,
                it.receiver,
                it.receiverWalletType,
                "",
                "",
                it.createDate
            )
        }).awaitFirstOrNull()
        return financialActions
    }

    override suspend fun updateStatus(financialAction: FinancialAction, status: FinancialActionStatus) {
        financialActionRepository.updateStatusAndIncreaseRetry(financialAction.id!!, status).awaitFirstOrNull()
    }
}