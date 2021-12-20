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
    override suspend fun calculate(uuid: String, richTrade: RichTrade): CommissionReward {
        if (uuid != richTrade.makerUuid && uuid != richTrade.takerUuid) throw IllegalArgumentException("Variable uuid is not in trade")
        val reference = referenceHandler.findByReferentUuid(uuid)
            ?: throw IllegalArgumentException("No referral entity found for uuid ($uuid)")
        val commission = if (uuid == richTrade.makerUuid) richTrade.makerCommision else richTrade.takerCommision
        return CommissionReward(
            reference.referralCode.uuid,
            reference.referentUuid,
            reference.referralCode.code,
            richTrade.id to richTrade,
            commission * (BigDecimal.ONE - reference.referralCode.referentCommission),
            commission * reference.referralCode.referentCommission
        )
    }
}
