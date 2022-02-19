package co.nilin.opex.admin.app.service

import co.nilin.opex.admin.app.data.AddCurrencyRequest
import co.nilin.opex.admin.core.events.AddCurrencyEvent
import co.nilin.opex.admin.core.spi.AdminEventPublisher
import org.springframework.stereotype.Service

@Service
class SystemConfigService(private val publisher: AdminEventPublisher) {

    suspend fun addCurrency(body: AddCurrencyRequest) {
        with(body) {
            publisher.publish(AddCurrencyEvent(name!!, symbol!!, precision))
        }
    }

}