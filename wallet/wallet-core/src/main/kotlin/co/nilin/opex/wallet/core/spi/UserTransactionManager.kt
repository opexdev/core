package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.model.UserTransaction
import co.nilin.opex.wallet.core.model.UserTransactionCategory
import co.nilin.opex.wallet.core.model.UserTransactionHistory
import java.time.LocalDateTime

interface UserTransactionManager {

    suspend fun save(tx: UserTransaction)

    suspend fun getTransactionHistoryForUser(
        userId: String,
        currency: String?,
        category: UserTransactionCategory?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        asc: Boolean,
        limit: Int,
        offset: Int
    ): List<UserTransactionHistory>
}