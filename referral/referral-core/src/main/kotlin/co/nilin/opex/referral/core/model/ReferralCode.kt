package co.nilin.opex.referral.core.model

import java.math.BigDecimal

data class ReferralCode(
    var uuid: String,
    var code: String,
    var referentCommission: BigDecimal
)
