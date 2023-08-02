package co.nilin.opex.kyc.app.config

import co.nilin.opex.kyc.core.spi.UserCreatedEventListener
import co.nilin.opex.kyc.ports.kafka.consumer.UserCreatedKafkaListener

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
open class AppConfig {
    @Autowired
    fun configureEventListeners(
            useCreatedKafkaListener: UserCreatedKafkaListener,
            userCreatedEventListener: UserCreatedEventListener,
    ) {
        useCreatedKafkaListener.addEventListener(userCreatedEventListener)

    }

}