package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.model.Transaction
import co.nilin.opex.wallet.core.model.TransactionHistory
import co.nilin.opex.wallet.core.model.TransactionWithDetailHistory
import co.nilin.opex.wallet.core.spi.TransactionManager
import co.nilin.opex.wallet.ports.postgres.dao.TransactionRepository
import co.nilin.opex.wallet.ports.postgres.model.TransactionModel
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class TransactionManagerImpl(
    private val transactionRepository: TransactionRepository,
    private val objectMapper: ObjectMapper
) : TransactionManager {
    private val logger = LoggerFactory.getLogger(TransactionManagerImpl::class.java)

    override suspend fun save(transaction: Transaction): String {
        return transactionRepository.save(
            TransactionModel(
                null,
                transaction.sourceWallet.id!!,
                transaction.destWallet.id!!,
                transaction.sourceAmount,
                transaction.destAmount,
                transaction.description,
                transaction.transferRef,
                transaction.transferCategory,
                objectMapper.writeValueAsString(transaction.additionalData),
                LocalDateTime.now()
            )
        ).awaitSingle().id.toString()
    }

    override suspend fun findDepositTransactions(
        uuid: String,
        coin: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
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
                    it.wallet,
                    it.amount,
                    it.description,
                    it.ref,
                    it.date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    it.category,
                    if (it.detail == null) emptyMap() else objectMapper.readValue(
                        it.detail,
                        Map::class.java
                    ) as Map<String, Any>?,
                )
            }
    }

    override suspend fun findWithdrawTransactions(
        uuid: String,
        coin: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
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
                    it.wallet,
                    it.amount,
                    it.description,
                    it.ref,
                    it.date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    it.category,
                    if (it.detail == null) emptyMap() else objectMapper.readValue(
                        it.detail,
                        Map::class.java
                    ) as Map<String, Any>?,
                )
            }
    }

    override suspend fun findTransactions(
        uuid: String,
        coin: String?,
        category: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        asc: Boolean,
        limit: Int,
        offset: Int
    ): List<TransactionWithDetailHistory> {
        val transactions =
            if (asc)
                transactionRepository.findTransactionsAsc(uuid, coin, category, startTime, endTime, limit, offset)
            else
                transactionRepository.findTransactionsDesc(uuid, coin, category, startTime, endTime, limit, offset)

        return transactions.collectList()
            .awaitFirstOrElse { emptyList() }
            .map {
                TransactionWithDetailHistory(
                    it.id,
                    it.srcWallet,
                    it.destWallet,
                    it.senderUuid,
                    it.receiverUuid,
                    it.currency,
                    it.amount,
                    it.description,
                    it.ref,
                    it.date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    it.category,
                    if (it.detail == null) emptyMap() else objectMapper.readValue(
                        it.detail,
                        Map::class.java
                    ) as Map<String, Any>?,
                    (it.senderUuid == uuid) && (it.senderUuid != it.receiverUuid)
                )
            }
    }
}