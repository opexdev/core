package co.nilin.opex.referral.app.controller

import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.referral.core.model.CheckoutState
import co.nilin.opex.referral.core.spi.CheckoutHandler
import co.nilin.opex.referral.core.spi.CheckoutRecordHandler
import co.nilin.opex.referral.core.spi.ConfigHandler
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.ZoneId
import java.util.*

@RestController
@RequestMapping("/checkouts")
class CheckoutController(
    private val checkoutHandler: CheckoutHandler,
    private val configHandler: ConfigHandler,
    private val checkoutRecordHandler: CheckoutRecordHandler
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

    @ApiOperation(
        value = "Checkout pending commissions",
        notes = "Checkout pending commissions."
    )
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example()
    )
    @PutMapping("/checkout-all")
    suspend fun checkoutAll() {
        val min = configHandler.findConfig("default")!!.minPaymentAmount
        checkoutHandler.checkoutEveryCandidate(min)
    }

    @ApiOperation(
        value = "Get all checkouts",
        notes = "Get all checkouts by status."
    )
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = """
                    [
                        {
                            "commissionRewardsId": 1,
                            "rewardedUuid": "b3e4f2bd-15c6-4912-bdef-161445a98193",
                            "referentUuid": "a5e510f9-bda8-4ecb-b500-0980f525dc52",
                            "referralCode": "10000",
                            "richTrade": 1,
                            "referentOrderDirection": "BID",
                            "share": 0.001,
                            "createDate": 1646213088,
                            "checkoutState": "PENDING",
                            "transferRef": "wallet-transaction-id",
                            "updateDate": 1646213088
                        }
                    ]
                """,
                mediaType = "application/json"
            )
        )
    )
    @GetMapping
    suspend fun get(@RequestParam status: CheckoutState): List<CheckoutRecordBody> {
        return checkoutRecordHandler.findCommissionsByCheckoutState(status).map {
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
