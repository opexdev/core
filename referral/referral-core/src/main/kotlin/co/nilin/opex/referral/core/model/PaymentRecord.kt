package co.nilin.opex.referral.core.model

import java.time.LocalDateTime

data class PaymentRecord(
    var commissionReward: CommissionReward,
    var paymentStatus: PaymentStatuses,
    var transferRef: String?,
    var updateDate: LocalDateTime
)
