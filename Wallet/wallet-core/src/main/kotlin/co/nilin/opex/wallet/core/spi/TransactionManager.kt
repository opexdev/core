package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.model.Transaction
import co.nilin.opex.wallet.core.model.TransactionHistory

interface TransactionManager {

    suspend fun save(transaction: Transaction): String

    suspend fun findDepositTransactions(
        uuid: String,
        coin:String?,
        startTime: Long,
        endTime: Long,
        limit: Int,
        offset: Int
    ): List<TransactionHistory>

    suspend fun findWithdrawTransactions(
        uuid: String,
        coin:String?,
        startTime: Long,
        endTime: Long,
        limit: Int,
        offset: Int
    ): List<TransactionHistory>
}