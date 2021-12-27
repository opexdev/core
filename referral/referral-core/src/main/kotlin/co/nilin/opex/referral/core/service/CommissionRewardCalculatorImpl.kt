package co.nilin.opex.referral.core.service

import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.referral.core.api.CommissionRewardCalculator
import co.nilin.opex.referral.core.api.SymbolPriceCalculator
import co.nilin.opex.referral.core.model.CommissionReward
import co.nilin.opex.referral.core.spi.ConfigHandler
import co.nilin.opex.referral.core.spi.ReferenceHandler
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class CommissionRewardCalculatorImpl(
    private val configHandler: ConfigHandler,
    private val symbolPriceCalculator: SymbolPriceCalculator,
    private val referenceHandler: ReferenceHandler
) : CommissionRewardCalculator {
    override suspend fun calculate(ouid: String, richTrade: RichTrade): CommissionReward {
        val config = configHandler.findConfig("default")!!
        if (ouid != richTrade.makerOuid && ouid != richTrade.takerOuid) throw IllegalArgumentException("Order is not correct")
        val uuid = if (ouid == richTrade.makerOuid) richTrade.makerUuid else richTrade.takerUuid
        val reference = referenceHandler.findByReferentUuid(uuid)
            ?: throw IllegalArgumentException("No referral entity found for uuid ($uuid)")
        val commission =
            if (ouid == richTrade.makerOuid) richTrade.makerCommision * symbolPriceCalculator.getPrice(richTrade.makerCommisionAsset)
            else richTrade.takerCommision * symbolPriceCalculator.getPrice(richTrade.takerCommisionAsset)
        val direction = if (ouid == richTrade.makerOuid) richTrade.makerDirection else richTrade.takerDirection
        return CommissionReward(
            reference.referralCode.uuid,
            reference.referentUuid,
            reference.referralCode.code,
            richTrade.id to richTrade,
            direction,
            commission * (BigDecimal.ONE - reference.referralCode.referentCommission),
            commission * reference.referralCode.referentCommission,
            config.paymentAssetSymbol
        )
    }
}
