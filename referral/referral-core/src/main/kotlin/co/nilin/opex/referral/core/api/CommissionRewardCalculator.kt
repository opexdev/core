package co.nilin.opex.referral.core.api

import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.referral.core.model.CommissionReward

interface CommissionRewardCalculator {
    suspend fun calculate(ouid: String, richTrade: RichTrade): List<CommissionReward>
}