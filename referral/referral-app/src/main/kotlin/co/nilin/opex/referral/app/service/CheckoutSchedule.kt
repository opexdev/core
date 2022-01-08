package co.nilin.opex.referral.app.service

import co.nilin.opex.referral.core.spi.CheckoutHandler
import co.nilin.opex.referral.core.spi.ConfigHandler
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.util.*

@Service
class CheckoutSchedule(private val checkoutHandler: CheckoutHandler, private val configHandler: ConfigHandler) {
    @Scheduled(fixedDelay = 12 * 60 * 60 * 1000)
    fun pay() {
        runBlocking {
            val config = configHandler.findConfig("default")!!
            val minDate = Date.from(Timestamp(Date().time - config.paymentWindowSeconds * 1000).toInstant())
            checkoutHandler.checkoutOlderThan(minDate)
        }
    }
}
