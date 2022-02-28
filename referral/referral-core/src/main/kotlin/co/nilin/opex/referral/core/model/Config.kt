package co.nilin.opex.referral.core.model

import java.math.BigDecimal

data class Config(
    var name: String,
    var referralCommissionReward: BigDecimal,
    var paymentCurrency: String,
    var minPaymentAmount: BigDecimal,
    var paymentWindowSeconds: Int,
    var maxReferralCodePerUser: Int
)
