package co.nilin.mixchange.eventlog.app.config

import co.nilin.mixchange.eventlog.spi.EventPersister
import co.nilin.mixchange.eventlog.spi.OrderPersister
import co.nilin.mixchange.eventlog.spi.TradePersister
import co.nilin.mixchange.matching.core.eventh.events.*
import co.nilin.mixchange.port.eventlog.kafka.consumer.OrderKafkaListener
import co.nilin.mixchange.port.eventlog.kafka.spi.OrderSubmitRequestListener
import co.nilin.mixchange.port.order.kafka.inout.OrderSubmitRequest
import co.nilin.mixchange.port.trade.consumer.EventKafkaListener
import co.nilin.mixchange.port.trade.consumer.TradeKafkaListener
import co.nilin.mixchange.port.trade.spi.EventListener
import co.nilin.mixchange.port.trade.spi.TradeListener
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
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
    fun eventlogEventListener(
        orderPersister: OrderPersister, eventPersister: EventPersister
    ): EventlogEventListener {
        return EventlogEventListener(orderPersister, eventPersister)
    }

    @Bean
    fun orderKafkaListener():OrderKafkaListener {
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

    class OrderListener(val orderPersister: OrderPersister) : OrderSubmitRequestListener {

        override fun id(): String {
            return "OrderListener"
        }

        override suspend fun onOrder(order: OrderSubmitRequest, partition: Int, offset: Long, timestamp: Long) {
            orderPersister.submitOrder(
                SubmitOrderEvent(
                    order.ouid,
                    order.uuid,
                    order.orderId,
                    order.pair,
                    order.price,
                    order.quantity,
                    0,
                    order.direction,
                    order.matchConstraint,
                    order.orderType
                )
            )
        }
    }

    class EventlogTradeListener(val tradePersister: TradePersister) : TradeListener {

        override fun id(): String {
            return "TradeListener"
        }

        override fun onTrade(tradeEvent: TradeEvent, partition: Int, offset: Long, timestamp: Long) {
            runBlocking {
                tradePersister.saveTrade(tradeEvent)
            }
        }
    }

    class EventlogEventListener(
        val orderPersister: OrderPersister, val eventPersister: EventPersister
    ) : EventListener {

        override fun id(): String {
            return "EventListener"
        }

        override fun onEvent(coreEvent: CoreEvent, partition: Int, offset: Long, timestamp: Long) {
            runBlocking {
                if (coreEvent is CreateOrderEvent)
                    orderPersister.saveOrder(coreEvent)
                else if (coreEvent is RejectOrderEvent)
                    orderPersister.rejectOrder(coreEvent)
                else if (coreEvent is UpdatedOrderEvent)
                    orderPersister.updateOrder(coreEvent)
                else if (coreEvent is CancelOrderEvent)
                    orderPersister.cancelOrder(coreEvent)
                eventPersister.saveEvent(coreEvent)
            }
            println("onEvent")
        }
    }
}