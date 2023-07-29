package co.nilin.opex.kyc.app.config

import co.nilin.opex.profile.ports.kafka.consumer.UserCreatedKafkaListener
import co.nilin.opex.profile.core.spi.UserCreatedEventListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {
    @Autowired
    fun configureEventListeners(
            useCreatedKafkaListener: UserCreatedKafkaListener,
            userCreatedEventListener: UserCreatedEventListener,
    ) {
        useCreatedKafkaListener.addEventListener(userCreatedEventListener)

    }

}