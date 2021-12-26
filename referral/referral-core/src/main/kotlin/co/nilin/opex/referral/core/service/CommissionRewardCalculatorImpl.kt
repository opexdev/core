package co.nilin.opex.referral.core.service

import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.referral.core.api.CommissionRewardCalculator
import co.nilin.opex.referral.core.model.CommissionReward
import co.nilin.opex.referral.core.spi.ReferenceHandler
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class CommissionRewardCalculatorImpl(
    private val referenceHandler: ReferenceHandler
) : CommissionRewardCalculator {
    override suspend fun calculate(ouid: String, richTrade: RichTrade): CommissionReward {
        if (ouid != richTrade.makerOuid && ouid != richTrade.takerOuid) throw IllegalArgumentException("Order is not correct")
        val uuid = if (ouid == richTrade.makerOuid) richTrade.makerUuid else richTrade.takerUuid
        val reference = referenceHandler.findByReferentUuid(uuid)
            ?: throw IllegalArgumentException("No referral entity found for uuid ($uuid)")
        val commission = if (ouid == richTrade.makerOuid) richTrade.makerCommision else richTrade.takerCommision
        val direction = if (ouid == richTrade.makerOuid) richTrade.makerDirection else richTrade.takerDirection
        return CommissionReward(
            reference.referralCode.uuid,
            reference.referentUuid,
            reference.referralCode.code,
            richTrade.id to richTrade,
            direction,
            commission * (BigDecimal.ONE - reference.referralCode.referentCommission),
            commission * reference.referralCode.referentCommission
        )
    }
}
