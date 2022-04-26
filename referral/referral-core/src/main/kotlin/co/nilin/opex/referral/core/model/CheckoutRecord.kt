package co.nilin.opex.referral.core.model

import java.time.LocalDateTime

data class CheckoutRecord(
    var commissionReward: CommissionReward,
    var checkoutState: CheckoutState,
    var transferRef: String?,
    var updateDate: LocalDateTime
)
