package co.nilin.opex.referral.core.api

import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.referral.core.model.CommissionReward

interface CommissionRewardCalculator {
    suspend fun calculate(uuid: String, richTrade: RichTrade): CommissionReward
}