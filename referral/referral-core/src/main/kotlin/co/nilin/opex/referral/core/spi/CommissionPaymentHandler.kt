package co.nilin.opex.referral.core.spi

import co.nilin.opex.referral.core.model.PaymentRecord
import co.nilin.opex.referral.core.model.PaymentStatuses
import java.math.BigDecimal

interface CommissionPaymentHandler {
    suspend fun findCommissionsByStatus(paymentStatus: PaymentStatuses): List<PaymentRecord>
    suspend fun findUserCommissionsWhereTotalGreaterAndEqualTo(uuid: String, value: BigDecimal): List<PaymentRecord>
    suspend fun findAllCommissionsWhereTotalGreaterAndEqualTo(value: BigDecimal): List<PaymentRecord>
    suspend fun findCommissionsWherePendingDateLessOrEqualThan(date: Long): List<PaymentRecord>
    suspend fun updatePaymentStatus(id: Long, value: PaymentStatuses)
    suspend fun checkout(id: Long, transferRef: String)
}
