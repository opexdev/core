package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.inout.TransactionSummary
import co.nilin.opex.wallet.core.model.UserTransaction
import co.nilin.opex.wallet.core.model.UserTransactionCategory
import co.nilin.opex.wallet.core.model.UserTransactionHistory
import co.nilin.opex.wallet.core.service.PrecisionService
import co.nilin.opex.wallet.core.spi.UserTransactionManager
import co.nilin.opex.wallet.ports.postgres.dao.UserTransactionRepository
import co.nilin.opex.wallet.ports.postgres.model.UserTransactionModel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class UserTransactionManagerImpl(
    private val repository: UserTransactionRepository,
    private val precisionService: PrecisionService
) : UserTransactionManager {

    override suspend fun save(tx: UserTransaction) {
        val txModel = with(tx) {
            UserTransactionModel(
                ownerId,
                txId,
                currency,
                balance,
                balanceChange,
                category,
                description
            )
        }
        repository.save(txModel).awaitSingleOrNull()
    }

    override suspend fun getTransactionHistory(
        userId: String?,
        currency: String?,
        category: UserTransactionCategory?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        asc: Boolean,
        limit: Int,
        offset: Int,
    ): List<UserTransactionHistory> {
        val transactions = if (asc)
            repository.findUserTransactionHistoryAsc(userId, currency, category, startTime, endTime, limit, offset)
        else
            repository.findUserTransactionHistoryDesc(userId, currency, category, startTime, endTime, limit, offset)

        return transactions
            .collectList()
            .awaitFirstOrElse { emptyList() }
            .onEach { transaction ->
                transaction.balance =
                    precisionService.calculatePrecision(transaction.balance, transaction.currency)
            }
    }

    override suspend fun getTransactionHistoryCount(
        userId: String?,
        currency: String?,
        category: UserTransactionCategory?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?
    ): Long {
        return repository.countByCriteria(userId, currency, category, startTime, endTime).awaitFirstOrElse { 0L }
    }

    override suspend fun getTradeTransactionSummary(
        uuid: String,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int?,
    ): List<TransactionSummary> {
        return repository.getTradeTransactionSummary(uuid, startTime, endTime, limit).toList()
    }
}