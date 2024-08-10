package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.model.UserTransaction
import co.nilin.opex.wallet.core.model.UserTransactionCategory
import co.nilin.opex.wallet.core.model.UserTransactionHistory
import co.nilin.opex.wallet.core.spi.UserTransactionManager
import co.nilin.opex.wallet.ports.postgres.dao.UserTransactionRepository
import co.nilin.opex.wallet.ports.postgres.model.UserTransactionModel
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class UserTransactionManagerImpl(private val repository: UserTransactionRepository) : UserTransactionManager {

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

    override suspend fun getTransactionHistoryForUser(
        userId: String,
        currency: String?,
        category: UserTransactionCategory?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        asc: Boolean,
        limit: Int,
        offset: Int
    ): List<UserTransactionHistory> {
        val transactions = if (asc)
            repository.findUserTransactionHistoryAsc(userId, currency, category, startTime, endTime, limit, offset)
        else
            repository.findUserTransactionHistoryDesc(userId, currency, category, startTime, endTime, limit, offset)

        return transactions.collectList().awaitFirstOrElse { emptyList() }
    }
}