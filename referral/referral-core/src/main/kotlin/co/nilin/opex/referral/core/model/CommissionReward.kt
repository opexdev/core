package co.nilin.opex.referral.core.model

import co.nilin.opex.accountant.core.inout.RichTrade

data class CommissionReward(
    var referrerUuid: String,
    var referentUuid: String,
    var referralCode: String,
    var richTrade: RichTrade
)
