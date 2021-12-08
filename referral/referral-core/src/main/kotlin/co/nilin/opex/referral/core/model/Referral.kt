package co.nilin.opex.referral.core.model

import java.math.BigDecimal

data class Referral(
    var id: Long?,
    var code: String,
    var referrerCommission: BigDecimal,
    var referentCommission: BigDecimal
)
