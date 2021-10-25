package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.WithdrawResponse
import co.nilin.opex.wallet.core.model.Withdraw

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

    suspend fun persist(withdraw: Withdraw):Withdraw
    suspend fun findById(withdrawId: String): Withdraw?
}