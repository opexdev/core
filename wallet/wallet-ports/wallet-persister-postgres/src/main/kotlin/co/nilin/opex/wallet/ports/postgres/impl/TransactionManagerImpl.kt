package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.model.Transaction
import co.nilin.opex.wallet.core.model.TransactionHistory
import co.nilin.opex.wallet.core.spi.TransactionManager
import co.nilin.opex.wallet.ports.postgres.dao.TransactionRepository
import co.nilin.opex.wallet.ports.postgres.model.TransactionModel
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId

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
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        limit: Int,
        offset: Int
    ): List<TransactionHistory> {
        val transactions = if (coin != null)
            transactionRepository.findDepositTransactionsByUUIDAndCurrency(uuid, coin, startTime, endTime, limit)
        else
            transactionRepository.findDepositTransactionsByUUID(uuid, startTime, endTime, limit)

        return transactions.collectList()
            .awaitFirstOrElse { emptyList() }
            .map {
                TransactionHistory(
                    it.id,
                    it.currency,
                    it.amount,
                    it.description,
                    it.ref,
                    it.date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                )
            }
    }

    override suspend fun findWithdrawTransactions(
        uuid: String,
        coin: String?,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        limit: Int,
        offset: Int
    ): List<TransactionHistory> {
        val transactions = if (coin != null)
            transactionRepository.findWithdrawTransactionsByUUIDAndCurrency(uuid, coin, startTime, endTime, limit)
        else
            transactionRepository.findWithdrawTransactionsByUUID(uuid, startTime, endTime, limit)

        return transactions.collectList()
            .awaitFirstOrElse { emptyList() }
            .map {
                TransactionHistory(
                    it.id,
                    it.currency,
                    it.amount,
                    it.description,
                    it.ref,
                    it.date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                )
            }
    }
}