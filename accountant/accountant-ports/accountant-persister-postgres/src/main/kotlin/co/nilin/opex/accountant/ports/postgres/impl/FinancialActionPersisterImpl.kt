package co.nilin.opex.accountant.ports.postgres.impl

import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.FinancialActionStatus
import co.nilin.opex.accountant.core.spi.FinancialActionPersister
import co.nilin.opex.accountant.ports.postgres.dao.FinancialActionRepository
import co.nilin.opex.accountant.ports.postgres.model.FinancialActionModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class FinancialActionPersisterImpl(private val financialActionRepository: FinancialActionRepository) :
    FinancialActionPersister {

    override suspend fun persist(financialActions: List<FinancialAction>): List<FinancialAction> {
        financialActionRepository.saveAll(financialActions.map {
            FinancialActionModel(
                null,
                it.uuid,
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
        }).collectList().awaitSingle()
        return financialActions
    }

    override suspend fun persistWithStatus(financialAction: FinancialAction, status: FinancialActionStatus) {
        financialActionRepository.save(
            with(financialAction) {
                FinancialActionModel(
                    null,
                    uuid,
                    parent?.id,
                    eventType,
                    pointer,
                    symbol,
                    amount,
                    sender,
                    senderWalletType,
                    receiver,
                    receiverWalletType,
                    "",
                    "",
                    createDate,
                    status
                )
            }
        ).awaitSingle()
    }


    override suspend fun updateStatus(financialAction: FinancialAction, status: FinancialActionStatus) {
        financialActionRepository.updateStatus(financialAction.id!!, status).awaitSingleOrNull()
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override suspend fun updateStatusNewTx(financialAction: FinancialAction, status: FinancialActionStatus) {
        financialActionRepository.updateStatus(financialAction.id!!, status).awaitSingleOrNull()
    }

    override suspend fun updateStatus(faUuid: String, status: FinancialActionStatus) {
        financialActionRepository.updateStatus(faUuid, status).awaitSingleOrNull()
    }

    override suspend fun updateBatchStatus(financialAction: List<FinancialAction>, status: FinancialActionStatus) {
        financialActionRepository.updateBatchStatus(financialAction.mapNotNull { it.id }, status).awaitFirstOrNull()
    }
}