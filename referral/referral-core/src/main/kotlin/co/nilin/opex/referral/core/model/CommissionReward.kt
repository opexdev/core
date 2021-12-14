package co.nilin.opex.referral.core.model

import co.nilin.opex.accountant.core.inout.RichTrade
import java.math.BigDecimal

data class CommissionReward(
    var referrerUuid: String,
    var referentUuid: String,
    var referralCode: String,
    var richTrade: RichTrade?,
    var referrerShare: BigDecimal,
    var referentShare: BigDecimal
)
