package co.nilin.opex.referral.core.model

data class PaymentRecord(
    var commissionReward: CommissionReward,
    var paymentStatus: PaymentStatuses
)
