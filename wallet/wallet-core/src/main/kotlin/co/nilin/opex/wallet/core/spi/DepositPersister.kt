package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.Deposit
import co.nilin.opex.wallet.core.inout.Deposits
import co.nilin.opex.wallet.core.inout.WithdrawResponse
import co.nilin.opex.wallet.core.model.Withdraw
import java.time.LocalDateTime

interface DepositPersister {

    suspend fun persist(deposit: Deposit): Deposit

    suspend fun findDepositHistory(
        uuid: String,
        currency: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean?
    ): List<Deposit>
}