package co.nilin.opex.accountant.ports.postgres.impl

import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.FinancialActionStatus
import co.nilin.opex.accountant.core.spi.FinancialActionPersister
import co.nilin.opex.accountant.core.spi.JsonMapper
import co.nilin.opex.accountant.ports.postgres.config.FinancialActionProperties
import co.nilin.opex.accountant.ports.postgres.dao.FinancialActionErrorRepository
import co.nilin.opex.accountant.ports.postgres.dao.FinancialActionRepository
import co.nilin.opex.accountant.ports.postgres.dao.FinancialActionRetryRepository
import co.nilin.opex.accountant.ports.postgres.model.FinancialActionErrorModel
import co.nilin.opex.accountant.ports.postgres.model.FinancialActionModel
import co.nilin.opex.accountant.ports.postgres.model.FinancialActionRetryModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class FinancialActionPersisterImpl(
    private val repository: FinancialActionRepository,
    private val faRetryRepository: FinancialActionRetryRepository,
    private val faErrorRepository: FinancialActionErrorRepository,
    private val faConfig: FinancialActionProperties,
    private val jsonMapper: JsonMapper
) : FinancialActionPersister {

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
                jsonMapper.serialize(it.detail),
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
                    jsonMapper.serialize(detail),
                    "",
                    "",
                    createDate,
                    status
                )
            }
        ).awaitSingle()
    }

    override suspend fun persistWithError(financialAction: FinancialAction, error: String, message: String?) {
        val faId = financialAction.id!!
        val retryModel = faRetryRepository.findByFaId(faId).awaitSingleOrNull()

        var status = FinancialActionStatus.RETRYING
        if (retryModel == null) {
            val runTime = LocalDateTime.now().plusSeconds(faConfig.retry.delaySeconds)
            faRetryRepository.save(FinancialActionRetryModel(faId, runTime)).awaitSingleOrNull()
        } else if (retryModel.isResolved || retryModel.hasGivenUp) {
            //Do nothing
        } else {
            faRetryRepository.save(retryModel.apply {
                retries += 1
                nextRunTime = LocalDateTime.now()
                    .plusSeconds(retries * faConfig.retry.delayMultiplier * faConfig.retry.delaySeconds)
                hasGivenUp = retries >= faConfig.retry.count
            }).awaitSingleOrNull()

            if (retryModel.hasGivenUp)
                status = FinancialActionStatus.ERROR
        }

        repository.updateStatus(faId, status).awaitSingleOrNull()

        val isRetrying = retryModel != null
        faErrorRepository.save(FinancialActionErrorModel(faId, error, message, isRetrying, retryModel?.id))
            .awaitSingleOrNull()
    }

    override suspend fun updateStatus(financialAction: FinancialAction, status: FinancialActionStatus) {
        repository.updateStatus(financialAction.id!!, status).awaitSingleOrNull()
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override suspend fun updateStatusNewTx(financialAction: FinancialAction, status: FinancialActionStatus) {
        repository.updateStatus(financialAction.id!!, status).awaitSingleOrNull()
    }

    override suspend fun updateStatus(faUuid: String, status: FinancialActionStatus) {
        repository.updateStatus(faUuid, status).awaitSingleOrNull()
    }

    override suspend fun updateBatchStatus(financialAction: List<FinancialAction>, status: FinancialActionStatus) {
        repository.updateBatchStatus(financialAction.mapNotNull { it.id }, status).awaitFirstOrNull()
    }
}