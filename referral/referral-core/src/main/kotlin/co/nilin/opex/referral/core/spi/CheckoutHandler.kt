package co.nilin.opex.referral.core.spi

import java.math.BigDecimal

interface CheckoutHandler {
    suspend fun checkoutById(uuid: String)
    suspend fun checkoutEveryCandidate(min: BigDecimal)
    suspend fun checkoutOlderThan(date: Long)
}
