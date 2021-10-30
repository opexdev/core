package co.nilin.opex.app.config

import co.nilin.opex.app.bl.ExchangeEventHandler
import co.nilin.opex.app.bl.OrderBooks
import co.nilin.opex.matching.core.eventh.events.*
import co.nilin.opex.matching.core.inout.OrderCancelCommand
import co.nilin.opex.matching.core.inout.OrderCreateCommand
import co.nilin.opex.matching.core.inout.OrderEditCommand
import co.nilin.opex.matching.core.model.PersistentOrderBook
import co.nilin.opex.matching.core.spi.OrderBookPersister
import co.nilin.opex.port.order.kafka.consumer.EventKafkaListener
import co.nilin.opex.port.order.kafka.consumer.OrderKafkaListener
import co.nilin.opex.port.order.kafka.inout.OrderSubmitRequest
import co.nilin.opex.port.order.kafka.spi.EventListener
import co.nilin.opex.port.order.kafka.spi.OrderSubmitRequestListener
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

    class OrderListener() : OrderSubmitRequestListener {

        override fun id(): String {
            return "OrderListener"
        }

        override suspend fun onOrder(order: OrderSubmitRequest, partition: Int, offset: Long, timestamp: Long) {
            val orderBook = OrderBooks.lookupOrderBook(
                order.pair.leftSideName + "_"
                        + order.pair.rightSideName
            )
            orderBook.handleNewOrderCommand(
                OrderCreateCommand(
                    order.ouid,
                    order.uuid,
                    order.pair,
                    order.price,
                    order.quantity,
                    order.direction,
                    order.matchConstraint,
                    order.orderType
                )
            )
        }
    }

    class MatchingEngineEventListener() : EventListener {

        private val logger = LoggerFactory.getLogger(MatchingEngineEventListener::class.java)

        override fun id(): String {
            return "EventListener"
        }

        override fun onEvent(event: CoreEvent, partition: Int, offset: Long, timestamp: Long) {
            logger.info("Received CoreEvent: ${event::class.java}")

            runBlocking(AppSchedulers.kafkaExecutor) {
                val orderBook = OrderBooks.lookupOrderBook("${event.pair.leftSideName}_${event.pair.rightSideName}")

                when (event) {
                    is EditOrderRequestEvent -> orderBook.handleEditCommand(
                        OrderEditCommand(
                            event.ouid,
                            event.uuid,
                            event.orderId,
                            event.pair,
                            event.price,
                            event.quantity
                        )
                    )

                    is CancelOrderEvent -> orderBook.handleCancelCommand(
                        OrderCancelCommand(
                            event.ouid,
                            event.uuid,
                            event.orderId,
                            event.pair
                        )
                    )
                    else -> null
                }
            }
        }
    }

}