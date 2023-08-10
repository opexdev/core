package co.nilin.opex.kyc.app.config

import co.nilin.opex.kyc.core.spi.UserCreatedEventListener
import co.nilin.opex.kyc.ports.kafka.eventlistener.consumer.UserCreatedKafkaListener
import co.nilin.opex.kyc.ports.postgres.imp.UserLevelAspect
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AppConfig {
    private val logger = LoggerFactory.getLogger(AppConfig::class.java)

    @Autowired
    fun configureEventListeners(

            useCreatedKafkaListener: UserCreatedKafkaListener,
            userCreatedEventListener: UserCreatedEventListener,
    ) {
        useCreatedKafkaListener.addEventListener(userCreatedEventListener)
    }




}