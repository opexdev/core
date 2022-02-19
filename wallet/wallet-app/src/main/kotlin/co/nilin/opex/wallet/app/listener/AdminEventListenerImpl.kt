package co.nilin.opex.wallet.app.listener

import co.nilin.opex.wallet.ports.kafka.listener.model.AddCurrencyEvent
import co.nilin.opex.wallet.ports.kafka.listener.model.AdminEvent
import co.nilin.opex.wallet.ports.kafka.listener.spi.AdminEventListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AdminEventListenerImpl : AdminEventListener {

    private val logger = LoggerFactory.getLogger(AdminEventListenerImpl::class.java)

    override fun id() = "AdminEventListener"

    override fun onEvent(event: AdminEvent, partition: Int, offset: Long, timestamp: Long) {
        when (event) {
            is AddCurrencyEvent -> {

            }
            else -> {}
        }
    }
}