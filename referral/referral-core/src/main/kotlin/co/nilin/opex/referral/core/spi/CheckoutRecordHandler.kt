package co.nilin.opex.referral.core.spi

import co.nilin.opex.referral.core.model.CheckoutRecord
import co.nilin.opex.referral.core.model.CheckoutState
import java.math.BigDecimal
import java.util.*

interface CheckoutRecordHandler {
    suspend fun findCommissionsByCheckoutState(checkoutState: CheckoutState): List<CheckoutRecord>
    suspend fun findUserCommissionsWhereTotalGreaterAndEqualTo(uuid: String, value: BigDecimal): List<CheckoutRecord>
    suspend fun findAllCommissionsWhereTotalGreaterAndEqualTo(value: BigDecimal): List<CheckoutRecord>
    suspend fun findCommissionsWherePendingDateLessOrEqualThan(date: Date): List<CheckoutRecord>
    suspend fun updateCheckoutState(id: Long, value: CheckoutState)
    suspend fun checkout(id: Long, transferRef: String)
}
