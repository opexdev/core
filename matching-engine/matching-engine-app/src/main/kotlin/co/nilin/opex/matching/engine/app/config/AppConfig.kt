package co.nilin.opex.matching.engine.app.config

import co.nilin.opex.matching.engine.app.bl.ExchangeEventHandler
import co.nilin.opex.matching.engine.app.bl.OrderBooks
import co.nilin.opex.matching.engine.app.listener.MatchingEngineEventListener
import co.nilin.opex.matching.engine.app.listener.OrderListener
import co.nilin.opex.matching.engine.core.eventh.events.CancelOrderEvent
import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent
import co.nilin.opex.matching.engine.core.eventh.events.EditOrderRequestEvent
import co.nilin.opex.matching.engine.core.inout.OrderCancelCommand
import co.nilin.opex.matching.engine.core.inout.OrderCreateCommand
import co.nilin.opex.matching.engine.core.inout.OrderEditCommand
import co.nilin.opex.matching.engine.core.model.PersistentOrderBook
import co.nilin.opex.matching.engine.core.spi.OrderBookPersister
import co.nilin.opex.matching.engine.ports.kafka.listener.consumer.EventKafkaListener
import co.nilin.opex.matching.engine.ports.kafka.listener.consumer.OrderKafkaListener
import co.nilin.opex.matching.engine.ports.kafka.listener.inout.OrderSubmitRequest
import co.nilin.opex.matching.engine.ports.kafka.listener.spi.EventListener
import co.nilin.opex.matching.engine.ports.kafka.listener.spi.OrderSubmitRequestListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {

    @Value("\${spring.app.symbols}")
    private val symbols: String? = null

    @Bean
    @ConditionalOnMissingBean(value = [OrderBookPersister::class])
    fun orderBookPersister(): OrderBookPersister {
        return object : OrderBookPersister {
            override suspend fun storeLastState(orderBook: PersistentOrderBook) {
            }

            override suspend fun loadLastState(symbol: String): PersistentOrderBook? {
                return null
            }
        }
    }

    @Autowired
    fun configureOrderBooks(orderBookPersister: OrderBookPersister) {
        symbols!!.split(",")
            .forEach { symbol ->
                CoroutineScope(AppSchedulers.generalExecutor).launch {
                    val lastOrderBook = orderBookPersister.loadLastState(symbol)
                    //todo: load db orders from last order in order book and put in order book
                    //todo: add missing orders to lastOrderBook or create one
                    if (lastOrderBook != null) {
                        withContext(coroutineContext) {
                            OrderBooks.reloadOrderBook(lastOrderBook)
                        }
                    } else {
                        OrderBooks.createOrderBook(symbol)
                    }

                }
            }
    }

    @Bean
    fun orderListener(): OrderListener {
        return OrderListener()
    }

    @Autowired
    fun configureOrderListener(orderKafkaListener: OrderKafkaListener, orderListener: OrderListener) {
        orderKafkaListener.addOrderListener(orderListener)
    }

    @Bean
    fun eventListener(): MatchingEngineEventListener {
        return MatchingEngineEventListener()
    }

    @Autowired
    fun configureEventListener(eventKafkaListener: EventKafkaListener, eventListener: MatchingEngineEventListener) {
        eventKafkaListener.addEventListener(eventListener)
    }

    @Autowired
    fun configureMatchingEngineListener(exchangeEventHandler: ExchangeEventHandler) {
        exchangeEventHandler.register()
    }

}