package co.nilin.opex.referral.core.model

import java.math.BigDecimal

data class Config(
    var name: String,
    var referralCommissionReward: BigDecimal,
    var paymentAssetSymbol: String,
    var minPaymentAmount: BigDecimal
)
