package co.nilin.opex.wallet.app.config

import co.nilin.opex.wallet.ports.kafka.listener.consumer.AdminEventKafkaListener
import co.nilin.opex.wallet.ports.kafka.listener.consumer.FinancialActionKafkaListener
import co.nilin.opex.wallet.ports.kafka.listener.consumer.ProfileUpdatedKafkaListener
import co.nilin.opex.wallet.ports.kafka.listener.consumer.UserCreatedKafkaListener
import co.nilin.opex.wallet.ports.kafka.listener.spi.AdminEventListener
import co.nilin.opex.wallet.ports.kafka.listener.spi.FinancialActionEventListener
import co.nilin.opex.wallet.ports.kafka.listener.spi.ProfileUpdatedEventListener
import co.nilin.opex.wallet.ports.kafka.listener.spi.UserCreatedEventListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!otc")
class AppConfig {

    @Autowired
    fun configureEventListeners(
        useCreatedKafkaListener: UserCreatedKafkaListener,
        userCreatedEventListener: UserCreatedEventListener,
        adminKafkaEventListener: AdminEventKafkaListener,
        adminEventListener: AdminEventListener,
        financialActionKafkaListener: FinancialActionKafkaListener,
        financialActionEventListener: FinancialActionEventListener,
        profileUpdatedKafkaListener: ProfileUpdatedKafkaListener,
        profileUpdatedEventListener: ProfileUpdatedEventListener,
    ) {
        useCreatedKafkaListener.addEventListener(userCreatedEventListener)
        adminKafkaEventListener.addEventListener(adminEventListener)
        financialActionKafkaListener.addEventListener(financialActionEventListener)
        profileUpdatedKafkaListener.addEventListener(profileUpdatedEventListener)
    }
}