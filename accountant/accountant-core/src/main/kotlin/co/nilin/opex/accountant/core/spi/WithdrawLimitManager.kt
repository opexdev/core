package co.nilin.opex.accountant.core.spi

import co.nilin.opex.accountant.core.model.WithdrawLimitConfig
import java.math.BigDecimal

interface WithdrawLimitManager {
    suspend fun canWithdraw(uuid: String, userLevel: String, currency: String, amount: BigDecimal): Boolean
    suspend fun getAll(): List<WithdrawLimitConfig>
}