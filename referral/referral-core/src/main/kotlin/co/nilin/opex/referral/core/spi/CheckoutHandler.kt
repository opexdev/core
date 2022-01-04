package co.nilin.opex.referral.core.spi

import java.math.BigDecimal
import java.util.*

interface CheckoutHandler {
    suspend fun checkoutById(uuid: String)
    suspend fun checkoutEveryCandidate(min: BigDecimal)
    suspend fun checkoutOlderThan(date: Date)
}
