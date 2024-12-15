package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.WithdrawResponse
import co.nilin.opex.wallet.core.model.Withdraw
import co.nilin.opex.wallet.core.model.WithdrawStatus
import java.time.LocalDateTime

interface WithdrawPersister {

    suspend fun persist(withdraw: Withdraw): Withdraw

    suspend fun findById(withdrawId: Long): Withdraw?

    suspend fun findWithdrawResponseById(withdrawId: Long): WithdrawResponse?

//    suspend fun findByCriteria(
//        ownerUuid: String?,
//        currency: String?,
//        destTxRef: String?,
//        destAddress: String?,
//        status: List<WithdrawStatus>
//    ): List<WithdrawResponse>

    suspend fun findByCriteria(
        ownerUuid: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        status: List<WithdrawStatus>,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        ascendingByTime: Boolean?,
        offset: Int,
        size: Int
    ): List<WithdrawResponse>

    suspend fun countByCriteria(
        ownerUuid: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        status: List<WithdrawStatus>
    ): Long

    suspend fun findWithdrawHistory(
        uuid: String,
        currency: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean?
    ): List<WithdrawResponse>
}