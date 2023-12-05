package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.model.Transaction
import co.nilin.opex.wallet.core.model.TransactionHistory
import java.time.LocalDateTime

interface TransactionManager {

    suspend fun save(transaction: Transaction): String

    suspend fun findDepositTransactions(
        uuid: String,
        coin: String?,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        limit: Int,
        offset: Int
    ): List<TransactionHistory>

    suspend fun findWithdrawTransactions(
        uuid: String,
        coin: String?,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        limit: Int,
        offset: Int
    ): List<TransactionHistory>

    suspend fun findTransactions(uuid: String, coin: String?, category: String?, startTime: LocalDateTime, endTime: LocalDateTime, asc: Boolean, limit: Int, offset: Int): List<TransactionHistory>
}