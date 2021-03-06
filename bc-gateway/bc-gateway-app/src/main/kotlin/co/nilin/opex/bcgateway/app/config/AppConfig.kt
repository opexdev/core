package co.nilin.opex.bcgateway.app.config


import co.nilin.opex.bcgateway.core.api.AssignAddressService
import co.nilin.opex.bcgateway.core.api.InfoService
import co.nilin.opex.bcgateway.core.service.AssignAddressServiceImpl
import co.nilin.opex.bcgateway.core.service.InfoServiceImpl
import co.nilin.opex.bcgateway.core.spi.AssignedAddressHandler
import co.nilin.opex.bcgateway.core.spi.CurrencyHandler
import co.nilin.opex.bcgateway.core.spi.ReservedAddressHandler
import co.nilin.opex.bcgateway.ports.kafka.listener.consumer.AdminEventKafkaListener
import co.nilin.opex.bcgateway.ports.kafka.listener.spi.AdminEventListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {

    @Bean
    fun assignAddressService(
        currencyHandler: CurrencyHandler,
        assignedAddressHandler: AssignedAddressHandler,
        reservedAddressHandler: ReservedAddressHandler
    ): AssignAddressService {
        return AssignAddressServiceImpl(currencyHandler, assignedAddressHandler, reservedAddressHandler)
    }

    @Bean
    fun infoService(): InfoService {
        return InfoServiceImpl()
    }

    @Autowired
    fun configureEventListeners(
        adminKafkaEventListener: AdminEventKafkaListener,
        adminEventListener: AdminEventListener,
    ) {
        adminKafkaEventListener.addEventListener(adminEventListener)
    }
}
