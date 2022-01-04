package co.nilin.opex.referral.app.service

import co.nilin.opex.referral.core.spi.CheckoutHandler
import co.nilin.opex.referral.core.spi.ConfigHandler
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class PaymentSchedule(private val checkoutHandler: CheckoutHandler, private val configHandler: ConfigHandler) {
    @Scheduled(fixedDelay = 12 * 60 * 60 * 1000)
    fun pay() {
        runBlocking {
            val config = configHandler.findConfig("default")!!
            val minDate = System.currentTimeMillis() / 1000
            checkoutHandler.checkoutOlderThan(minDate)
        }
    }
}
