package co.nilin.opex.referral.core.spi

import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.referral.core.model.CommissionReward

interface CommissionRewardCalculator {
    suspend fun calculate(richTrade: RichTrade): CommissionReward
}