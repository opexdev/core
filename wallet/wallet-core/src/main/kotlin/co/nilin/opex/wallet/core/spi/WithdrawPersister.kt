package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.WithdrawResponse
import co.nilin.opex.wallet.core.model.Withdraw
import java.time.LocalDateTime

interface WithdrawPersister {

    suspend fun findByCriteria(
        ownerUuid: String?,
        withdrawId: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        noStatus: Boolean,
        status: List<String>?
    ): List<WithdrawResponse>

    suspend fun countByCriteria(
        ownerUuid: String?,
        withdrawId: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        noStatus: Boolean,
        status: List<String>?
    ): Long

    suspend fun findByCriteria(
        ownerUuid: String?,
        withdrawId: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        noStatus: Boolean,
        status: List<String>?,
        offset: Int,
        size: Int
    ): List<WithdrawResponse>

    suspend fun persist(withdraw: Withdraw): Withdraw

    suspend fun findById(withdrawId: String): Withdraw?

    suspend fun findWithdrawHistory(
        uuid: String,
        coin: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int
    ): List<Withdraw>
}