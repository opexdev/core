package co.nilin.opex.referral.core.service

import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.referral.core.api.CommissionRewardCalculator
import co.nilin.opex.referral.core.model.CommissionReward

class CommissionRewardCalculatorImpl : CommissionRewardCalculator {
    override suspend fun calculate(richTrade: RichTrade): CommissionReward {
        TODO("Not yet implemented")
    }
}