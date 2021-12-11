package co.nilin.opex.referral.core.service

import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.referral.core.api.CommissionRewardCalculator
import co.nilin.opex.referral.core.model.CommissionReward
import co.nilin.opex.referral.core.spi.ReferralCodeHandler
import org.springframework.stereotype.Service

@Service
class CommissionRewardCalculatorImpl(
    private val referralCodeHandler: ReferralCodeHandler
) : CommissionRewardCalculator {
    override suspend fun calculate(uuid: String, richTrade: RichTrade): CommissionReward {
        if (uuid != richTrade.makerUuid && uuid != richTrade.takerUuid) throw IllegalArgumentException("Variable uuid is not in trade")
        val rc = referralCodeHandler.findReferralByUuid(uuid)
            ?: throw IllegalArgumentException("No referral entity found for uuid ($uuid)")
        val commission = if (uuid == richTrade.makerUuid) richTrade.makerCommision else richTrade.takerCommision
        return CommissionReward(
            rc.referrerUuid,
            rc.referentUuid,
            rc.code,
            richTrade,
            commission * rc.referrerCommission,
            commission * rc.referentCommission
        )
    }
}
