package co.nilin.opex.referral.core.model

import java.math.BigDecimal

data class Referral(
    var code: String,
    var referrerUuid: String,
    var referentUuid: String,
    var referrerCommission: BigDecimal,
    var referentCommission: BigDecimal
)
