package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.model.WithdrawOtp

interface WithdrawOtpPersister {

    suspend fun save(withdrawOtp: WithdrawOtp)
    suspend fun findByWithdrawId(withdrawUuid: String): List<WithdrawOtp>
}
