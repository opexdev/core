package co.nilin.opex.referral.app.controller

import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.referral.core.model.CheckoutState
import co.nilin.opex.referral.core.spi.CheckoutHandler
import co.nilin.opex.referral.core.spi.CommissionPaymentHandler
import co.nilin.opex.referral.core.spi.ConfigHandler
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.ZoneId
import java.util.*

@RestController
@RequestMapping("/checkouts")
class CheckoutController(
    private val checkoutHandler: CheckoutHandler,
    private val configHandler: ConfigHandler,
    private val paymentHandler: CommissionPaymentHandler
) {
    data class CheckoutRecordBody(
        var commissionRewardsId: Long,
        var rewardedUuid: String,
        var referentUuid: String,
        var referralCode: String,
        var richTrade: Long,
        var referentOrderDirection: OrderDirection,
        var share: BigDecimal,
        var createDate: Date,
        var checkoutState: CheckoutState,
        var transferRef: String?,
        var updateDate: Date
    )

    @PutMapping("/checkout-all")
    suspend fun checkoutAll() {
        val min = configHandler.findConfig("default")!!.minPaymentAmount
        checkoutHandler.checkoutEveryCandidate(min)
    }

    @GetMapping
    suspend fun get(@RequestParam status: CheckoutState): List<CheckoutRecordBody> {
        return paymentHandler.findCommissionsByCheckoutState(status).map {
            CheckoutRecordBody(
                it.commissionReward.id,
                it.commissionReward.rewardedUuid,
                it.commissionReward.referentUuid,
                it.commissionReward.referralCode,
                it.commissionReward.richTrade.first,
                it.commissionReward.referentOrderDirection,
                it.commissionReward.share,
                Date.from(it.commissionReward.createDate.atZone(ZoneId.systemDefault()).toInstant()),
                it.checkoutState,
                it.transferRef,
                Date.from(it.updateDate.atZone(ZoneId.systemDefault()).toInstant())
            )
        }
    }
}
