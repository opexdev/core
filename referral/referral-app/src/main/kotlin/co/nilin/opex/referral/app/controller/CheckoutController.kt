package co.nilin.opex.referral.app.controller

import co.nilin.opex.referral.core.spi.CheckoutHandler
import co.nilin.opex.referral.core.spi.ConfigHandler
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("checkouts")
class CheckoutController(private val checkoutHandler: CheckoutHandler, private val configHandler: ConfigHandler) {
    @PutMapping("/all")
    suspend fun checkoutAll() {
        val min = configHandler.findConfig("default")!!.minPaymentAmount
        checkoutHandler.checkoutEveryCandidate(min)
    }
}
