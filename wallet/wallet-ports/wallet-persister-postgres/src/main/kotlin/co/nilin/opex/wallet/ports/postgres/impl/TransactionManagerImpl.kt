package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.spi.TransactionManager
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepositoryV2
import co.nilin.opex.wallet.ports.postgres.dao.TransactionRepository
import co.nilin.opex.wallet.ports.postgres.model.TransactionModel
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class TransactionManagerImpl(
    private val transactionRepository: TransactionRepository,
    private val currencyRepositoryV2: CurrencyRepositoryV2,
    private val objectMapper: ObjectMapper
) : TransactionManager {
    private val logger = LoggerFactory.getLogger(TransactionManagerImpl::class.java)

    override suspend fun save(transaction: Transaction): Long {
        return transactionRepository.save(
            TransactionModel(
                null,
                transaction.sourceWallet.id!!,
                transaction.destWallet.id!!,
                transaction.sourceAmount,
                transaction.destAmount,
                transaction.description,
                transaction.transferRef,
                transaction.transferCategory
            )
        ).awaitSingle().id!!
    }


    override suspend fun findDepositTransactions(
        uuid: String,
        coin: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean?
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
                    it.category
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
                    it.category
                )
            }
    }

    override suspend fun findTransactions(
        uuid: String,
        coin: String?,
        category: TransferCategory?,
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
                    it.srcWalletType,
                    it.destWalletType,
                    it.senderUuid,
                    it.receiverUuid,
                    it.currency,
                    it.amount,
                    it.description,
                    it.ref,
                    it.date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    it.category,
                    (it.senderUuid == uuid) && (it.senderUuid != it.receiverUuid)
                )
            }
    }


}



