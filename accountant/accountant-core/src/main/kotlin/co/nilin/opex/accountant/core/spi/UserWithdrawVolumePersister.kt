package co.nilin.opex.accountant.core.spi

import co.nilin.opex.accountant.core.model.WithdrawStatus
import java.math.BigDecimal
import java.time.LocalDateTime

interface UserWithdrawVolumePersister {
    suspend fun update(
        userId: String,
        currency: String,
        amount: BigDecimal,
        date: LocalDateTime,
        withdrawStatus: WithdrawStatus
    )

    suspend fun getTotalValueByUserAndDateAfter(uuid: String, startDate: LocalDateTime): BigDecimal
}