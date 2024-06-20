package co.nilin.opex.bcgateway.app.listener

import co.nilin.opex.bcgateway.app.service.AdminService
import co.nilin.opex.bcgateway.ports.kafka.listener.model.AddCurrencyEvent
import co.nilin.opex.bcgateway.ports.kafka.listener.model.AdminEvent
import co.nilin.opex.bcgateway.ports.kafka.listener.model.DeleteCurrencyEvent
import co.nilin.opex.bcgateway.ports.kafka.listener.model.EditCurrencyEvent
import co.nilin.opex.bcgateway.ports.kafka.listener.spi.AdminEventListener
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AdminEventListenerImpl(private val adminService: AdminService) : AdminEventListener {

    private val logger = LoggerFactory.getLogger(AdminEventListenerImpl::class.java)

    override fun id() = "AdminEventListener"

    override fun onEvent(event: AdminEvent, partition: Int, offset: Long, timestamp: Long): Unit = runBlocking {
        //todo check with peyman
        logger.info("Incoming admin event $event")
        when (event) {
//            is AddCurrencyEvent -> adminService.addCurrency(event.name, event.symbol)
//            is EditCurrencyEvent -> adminService.editCurrency(event.name, event.symbol)
//            is DeleteCurrencyEvent -> adminService.deleteCurrency(event.name)
            else -> {}
        }
    }
    
}