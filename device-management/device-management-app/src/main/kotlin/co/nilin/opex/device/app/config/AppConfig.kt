package co.nilin.opex.device.app.config

import co.nilin.opex.device.core.spi.LoginEventListener
import co.nilin.opex.device.core.spi.LogoutEventListener
import co.nilin.opex.device.ports.kafka.consumer.LoginKafkaListener
import co.nilin.opex.device.ports.kafka.consumer.LogoutKafkaListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration


@Configuration
class AppConfig {
    @Autowired
    fun configureEventListeners(
        loginKafkaListener: LoginKafkaListener,
        loginEventListener: LoginEventListener,
        logoutKafkaListener: LogoutKafkaListener,
        logoutEventListener: LogoutEventListener,
    ) {
        loginKafkaListener.addEventListener(loginEventListener)
        logoutKafkaListener.addEventListener(logoutEventListener)

    }
}