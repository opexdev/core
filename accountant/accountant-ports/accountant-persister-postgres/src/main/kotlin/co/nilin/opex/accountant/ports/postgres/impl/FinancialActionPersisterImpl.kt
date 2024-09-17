package co.nilin.opex.accountant.ports.postgres.impl

import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.FinancialActionStatus
import co.nilin.opex.accountant.core.spi.FinancialActionPersister
import co.nilin.opex.accountant.core.spi.JsonMapper
import co.nilin.opex.accountant.ports.postgres.dao.FinancialActionErrorRepository
import co.nilin.opex.accountant.ports.postgres.dao.FinancialActionRepository
import co.nilin.opex.accountant.ports.postgres.dao.FinancialActionRetryRepository
import co.nilin.opex.accountant.ports.postgres.model.FinancialActionErrorModel
import co.nilin.opex.accountant.ports.postgres.model.FinancialActionModel
import co.nilin.opex.accountant.ports.postgres.model.FinancialActionRetryModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class FinancialActionPersisterImpl(
    private val repository: FinancialActionRepository,
    private val faRetryRepository: FinancialActionRetryRepository,
    private val faErrorRepository: FinancialActionErrorRepository
) : FinancialActionPersister {

    @Value("\${app.fi-action.retry.count}")
    private var retryCount: Int = 5

    @Value("\${app.fi-action.retry.delay-seconds}")
    private var delaySeconds: Long = 4

    @Value("\${app.fi-action.retry.delay-multiplier}")
    private var delayMultiplier: Int = 3

    override suspend fun persist(financialActions: List<FinancialAction>): List<FinancialAction> {
        repository.saveAll(financialActions.map {
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
                it.category,
                "",
                "",
                it.createDate
            )
        }).collectList().awaitSingle()
        return financialActions
    }

    override suspend fun persistWithStatus(financialAction: FinancialAction, status: FinancialActionStatus) {
        repository.save(
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
                    category,
                    "",
                    "",
                    createDate,
                    status
                )
            }
        ).awaitSingle()
    }

    override suspend fun updateWithError(
        financialAction: FinancialAction,
        error: String,
        message: String?,
        body: String?
    ) {
        val faId = financialAction.id!!
        val retryModel = faRetryRepository.findByFaId(faId).awaitSingleOrNull()

        var status = FinancialActionStatus.RETRYING
        if (retryModel == null) {
            val runTime = LocalDateTime.now().plusSeconds(delaySeconds)
            faRetryRepository.save(FinancialActionRetryModel(faId, runTime)).awaitSingleOrNull()
        } else if (retryModel.isResolved || retryModel.hasGivenUp) {
            //Do nothing
        } else {
            with(retryModel) {
                val giveUp = retries + 1 >= retryCount
                faRetryRepository.scheduleNext(
                    id!!,
                    retries + 1,
                    LocalDateTime.now().plusSeconds(retries * delayMultiplier * delaySeconds),
                    giveUp
                ).awaitSingleOrNull()

                if (giveUp)
                    status = FinancialActionStatus.ERROR
            }
        }

        repository.updateStatus(faId, status).awaitSingleOrNull()

        faErrorRepository.save(FinancialActionErrorModel(faId, error, message, body, retryModel?.id))
            .awaitSingleOrNull()
    }

    override suspend fun updateStatus(financialAction: FinancialAction, status: FinancialActionStatus) {
        repository.updateStatus(financialAction.id!!, status).awaitSingleOrNull()
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override suspend fun updateStatusNewTx(financialAction: FinancialAction, status: FinancialActionStatus) {
        repository.updateStatus(financialAction.id!!, status).awaitSingleOrNull()
    }

    override suspend fun retrySuccessful(financialAction: FinancialAction) {
        faRetryRepository.updateResolvedTrue(financialAction.id!!).awaitSingleOrNull()
    }

    override suspend fun updateStatus(faUuid: String, status: FinancialActionStatus) {
        repository.updateStatus(faUuid, status).awaitSingleOrNull()
    }

    override suspend fun updateBatchStatus(financialAction: List<FinancialAction>, status: FinancialActionStatus) {
        repository.updateBatchStatus(financialAction.mapNotNull { it.id }, status).awaitFirstOrNull()
    }
}