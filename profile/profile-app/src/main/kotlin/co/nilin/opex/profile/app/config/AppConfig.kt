package co.nilin.opex.profile.app.config

import co.nilin.opex.profile.core.spi.KycLevelUpdatedEventListener
import co.nilin.opex.profile.core.spi.UserCreatedEventListener
import co.nilin.opex.profile.ports.kafka.consumer.KycLevelUpdatedKafkaListener
import co.nilin.opex.profile.ports.kafka.consumer.UserCreatedKafkaListener
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource


@Configuration
class AppConfig {
    @Autowired
    fun configureEventListeners(
            useCreatedKafkaListener: UserCreatedKafkaListener,
            userCreatedEventListener: UserCreatedEventListener,
            kycLevelUpdatedKafkaListener: KycLevelUpdatedKafkaListener,
            kycLevelUpdatedEventListener: KycLevelUpdatedEventListener
    ) {
        useCreatedKafkaListener.addEventListener(userCreatedEventListener)
        kycLevelUpdatedKafkaListener.addEventListener(kycLevelUpdatedEventListener)

    }


}