package co.nilin.opex.referral.core.spi

import co.nilin.opex.referral.core.model.PaymentRecord
import co.nilin.opex.referral.core.model.PaymentStatuses

interface CommissionPaymentHandler {
    suspend fun findCommissionsByStatus(paymentStatus: PaymentStatuses): List<PaymentRecord>
    suspend fun updatePaymentStatus(id: Long, value: PaymentStatuses)
}
