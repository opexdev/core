package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.TransactionSummary
import co.nilin.opex.wallet.core.inout.WithdrawAdminResponse
import co.nilin.opex.wallet.core.inout.WithdrawResponse
import co.nilin.opex.wallet.core.model.Withdraw
import co.nilin.opex.wallet.core.model.WithdrawStatus
import java.time.LocalDateTime

interface WithdrawPersister {

    suspend fun persist(withdraw: Withdraw): Withdraw

    suspend fun findByWithdrawUuid(withdrawUuid: String): Withdraw?

    suspend fun findWithdrawResponseById(withdrawUuid: String): WithdrawResponse?

    suspend fun findByCriteria(
        ownerUuid: String?,
        withdrawUuid: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        status: List<WithdrawStatus>,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        ascendingByTime: Boolean?,
        offset: Int,
        size: Int,
    ): List<WithdrawAdminResponse>

    suspend fun countByCriteria(
        ownerUuid: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        status: List<WithdrawStatus>,
    ): Long

    suspend fun findWithdrawHistory(
        uuid: String,
        currency: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean?,
        status: WithdrawStatus?
    ): List<WithdrawResponse>

    suspend fun findWithdrawHistoryCount(
        uuid: String,
        currency: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        status: WithdrawStatus?
    ): Long

    suspend fun getWithdrawSummary(
        uuid: String,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int?,
    ): List<TransactionSummary>
}