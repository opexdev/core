package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.model.*
import java.time.LocalDateTime

interface TransactionManager {

    suspend fun save(transaction: Transaction): Long

    suspend fun findDepositTransactions(
        uuid: String,
        coin: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean? = false
    ): List<TransactionHistory>

    suspend fun findWithdrawTransactions(
        uuid: String,
        coin: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int
    ): List<TransactionHistory>

    suspend fun findTransactions(
        uuid: String,
        coin: String?,
        category: TransferCategory?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        asc: Boolean,
        limit: Int,
        offset: Int
    ): List<TransactionWithDetailHistory>

    suspend fun findTradesForAdmin(
        coin: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        asc: Boolean,
        limit: Int,
        offset: Int
    ): List<TradeAdminResponse>
}