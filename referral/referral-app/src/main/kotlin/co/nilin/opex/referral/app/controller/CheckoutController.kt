package co.nilin.opex.referral.app.controller

import co.nilin.opex.referral.core.model.PaymentRecord
import co.nilin.opex.referral.core.model.PaymentStatuses
import co.nilin.opex.referral.core.spi.CheckoutHandler
import co.nilin.opex.referral.core.spi.CommissionPaymentHandler
import co.nilin.opex.referral.core.spi.ConfigHandler
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/checkouts")
class CheckoutController(
    private val checkoutHandler: CheckoutHandler,
    private val configHandler: ConfigHandler,
    private val paymentHandler: CommissionPaymentHandler
) {
    @PutMapping("/checkout-all")
    suspend fun checkoutAll() {
        val min = configHandler.findConfig("default")!!.minPaymentAmount
        checkoutHandler.checkoutEveryCandidate(min)
    }

    @GetMapping
    suspend fun get(@RequestParam status: PaymentStatuses): List<PaymentRecord> {
        return paymentHandler.findCommissionsByStatus(status)
    }
}
