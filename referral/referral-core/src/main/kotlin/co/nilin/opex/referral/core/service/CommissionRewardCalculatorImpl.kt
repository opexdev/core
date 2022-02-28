package co.nilin.opex.referral.core.service

import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.referral.core.api.CommissionRewardCalculator
import co.nilin.opex.referral.core.api.SymbolPriceCalculator
import co.nilin.opex.referral.core.model.CommissionReward
import co.nilin.opex.referral.core.spi.ReferenceHandler
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class CommissionRewardCalculatorImpl(
    private val symbolPriceCalculator: SymbolPriceCalculator,
    private val referenceHandler: ReferenceHandler
) : CommissionRewardCalculator {
    override suspend fun calculate(ouid: String, richTrade: RichTrade): List<CommissionReward> {
        if (ouid != richTrade.makerOuid && ouid != richTrade.takerOuid) throw IllegalArgumentException("Order is not correct")
        val uuid = if (ouid == richTrade.makerOuid) richTrade.makerUuid else richTrade.takerUuid
        val reference = referenceHandler.findByReferentUuid(uuid)
            ?: return emptyList()
        val commission =
            if (ouid == richTrade.makerOuid) richTrade.makerCommision * symbolPriceCalculator.getPrice(richTrade.makerCommisionAsset)
            else richTrade.takerCommision * symbolPriceCalculator.getPrice(richTrade.takerCommisionAsset)
        val direction = if (ouid == richTrade.makerOuid) richTrade.makerDirection else richTrade.takerDirection
        val ret = mutableListOf<CommissionReward>()
        if (commission > BigDecimal.ZERO) {
            if (reference.referralCode.referentCommission < BigDecimal.ONE) {
                ret.add(
                    CommissionReward(
                        0,
                        reference.referralCode.uuid,
                        reference.referentUuid,
                        reference.referralCode.code,
                        richTrade.id to richTrade,
                        direction,
                        commission * (BigDecimal.ONE - reference.referralCode.referentCommission),
                        LocalDateTime.now()
                    )
                )
            }
            if (reference.referralCode.referentCommission > BigDecimal.ZERO) {
                ret.add(
                    CommissionReward(
                        0,
                        reference.referentUuid,
                        reference.referentUuid,
                        reference.referralCode.code,
                        richTrade.id to richTrade,
                        direction,
                        commission * reference.referralCode.referentCommission,
                        LocalDateTime.now()
                    )
                )
            }
        }
        return ret
    }
}
