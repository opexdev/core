package co.nilin.opex.eventlog.app.config

import co.nilin.opex.eventlog.app.listeners.DeadLetterListener
import co.nilin.opex.eventlog.app.listeners.EventlogEventListener
import co.nilin.opex.eventlog.app.listeners.EventlogTradeListener
import co.nilin.opex.eventlog.app.listeners.OrderListener
import co.nilin.opex.eventlog.core.spi.DeadLetterPersister
import co.nilin.opex.eventlog.core.spi.EventPersister
import co.nilin.opex.eventlog.core.spi.OrderPersister
import co.nilin.opex.eventlog.core.spi.TradePersister
import co.nilin.opex.eventlog.ports.kafka.listener.consumer.DLTKafkaListener
import co.nilin.opex.eventlog.ports.kafka.listener.consumer.EventKafkaListener
import co.nilin.opex.eventlog.ports.kafka.listener.consumer.OrderKafkaListener
import co.nilin.opex.eventlog.ports.kafka.listener.consumer.TradeKafkaListener
import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors

@Configuration
class AppConfig {

    @Bean
    fun orderListener(orderPersister: OrderPersister): OrderListener {
        return OrderListener(orderPersister)
    }

    @Bean
    fun eventlogTradeListener(tradePersister: TradePersister): EventlogTradeListener {
        return EventlogTradeListener(tradePersister)
    }

    @Bean
    fun deadLetterListener(persister: DeadLetterPersister): DeadLetterListener {
        return DeadLetterListener(persister)
    }

    @Bean
    fun eventlogEventListener(orderPersister: OrderPersister, eventPersister: EventPersister): EventlogEventListener {
        return EventlogEventListener(orderPersister, eventPersister)
    }

    @Bean
    fun orderKafkaListener(): OrderKafkaListener {
        return OrderKafkaListener(Executors.newFixedThreadPool(10).asCoroutineDispatcher())
    }

    @Autowired
    fun configureOrderListener(orderKafkaListener: OrderKafkaListener, orderListener: OrderListener) {
        orderKafkaListener.addOrderListener(orderListener)
    }

    @Autowired
    fun configureTradeListener(tradeKafkaListener: TradeKafkaListener, eventlogTradeListener: EventlogTradeListener) {
        tradeKafkaListener.addTradeListener(eventlogTradeListener)
    }

    @Autowired
    fun configureEventListener(eventKafkaListener: EventKafkaListener, eventlogEventListener: EventlogEventListener) {
        eventKafkaListener.addEventListener(eventlogEventListener)
    }

    @Autowired
    fun configureDeadLetterListener(dltKafkaListener: DLTKafkaListener, deadLetterListener: DeadLetterListener) {
        dltKafkaListener.addEventListener(deadLetterListener)
    }

}