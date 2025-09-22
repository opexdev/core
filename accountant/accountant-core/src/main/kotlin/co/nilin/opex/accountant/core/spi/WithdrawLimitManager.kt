package co.nilin.opex.accountant.core.spi

import java.math.BigDecimal

interface WithdrawLimitManager {
    suspend fun canWithdraw(uuid: String, userLevel: String, currency: String, amount: BigDecimal): Boolean
}