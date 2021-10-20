package co.nilin.opex.port.wallet.postgres.impl

import co.nilin.opex.port.wallet.postgres.dao.TransactionRepository
import co.nilin.opex.port.wallet.postgres.model.TransactionModel
import co.nilin.opex.wallet.core.model.Transaction
import co.nilin.opex.wallet.core.model.TransactionHistory
import co.nilin.opex.wallet.core.spi.TransactionManager
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TransactionManagerImpl(val transactionRepository: TransactionRepository) : TransactionManager {

    override suspend fun save(transaction: Transaction): String {
        return transactionRepository.save(
            TransactionModel(
                null,
                transaction.sourceWallet.id()!!,
                transaction.destWallet.id()!!,
                transaction.sourceAmount,
                transaction.destAmount,
                transaction.description,
                transaction.transferRef,
                LocalDateTime.now()
            )
        ).awaitSingle().id.toString()
    }

    override suspend fun findDepositTransactions(
        uuid: String,
        coin: String?,
        startTime: Long,
        endTime: Long,
        limit: Int,
        offset: Int
    ): List<TransactionHistory> {
        return (if (coin != null) transactionRepository.findDepositTransactionsByUUIDAndCurrency(
            uuid,
            coin
        ) else transactionRepository.findDepositTransactionsByUUID(uuid))
            .collectList()
            .awaitFirstOrElse { emptyList() }
            .map {
                TransactionHistory(it.id, it.currency, it.amount, it.description, it.date)
            }
    }

    override suspend fun findWithdrawTransactions(
        uuid: String,
        coin: String?,
        startTime: Long,
        endTime: Long,
        limit: Int,
        offset: Int
    ): List<TransactionHistory> {
        return (if (coin != null) transactionRepository.findWithdrawTransactionsByUUIDAndCurrency(
            uuid,
            coin
        ) else transactionRepository.findWithdrawTransactionsByUUID(uuid))
            .collectList()
            .awaitFirstOrElse { emptyList() }
            .map {
                TransactionHistory(it.id, it.currency, it.amount, it.description, it.date)
            }
    }
}