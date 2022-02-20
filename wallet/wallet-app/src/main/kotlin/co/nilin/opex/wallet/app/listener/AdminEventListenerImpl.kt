package co.nilin.opex.wallet.app.listener

import co.nilin.opex.wallet.core.spi.CurrencyService
import co.nilin.opex.wallet.ports.kafka.listener.model.AddCurrencyEvent
import co.nilin.opex.wallet.ports.kafka.listener.model.AdminEvent
import co.nilin.opex.wallet.ports.kafka.listener.model.DeleteCurrencyEvent
import co.nilin.opex.wallet.ports.kafka.listener.model.EditCurrencyEvent
import co.nilin.opex.wallet.ports.kafka.listener.spi.AdminEventListener
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AdminEventListenerImpl(private val currencyService: CurrencyService) : AdminEventListener {

    private val logger = LoggerFactory.getLogger(AdminEventListenerImpl::class.java)

    override fun id() = "AdminEventListener"

    override fun onEvent(event: AdminEvent, partition: Int, offset: Long, timestamp: Long): Unit = runBlocking {
        logger.info("Incoming admin event $event")
        when (event) {
            is AddCurrencyEvent -> currencyService.addCurrency(event.name, event.symbol, event.precision)
            is EditCurrencyEvent -> currencyService.editCurrency(event.name, event.symbol, event.precision)
            is DeleteCurrencyEvent -> currencyService.deleteCurrency(event.name)
            else -> {}
        }
    }
}