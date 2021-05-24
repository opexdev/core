package co.nilin.mixchange.app.config

import co.nilin.mixchange.accountant.core.api.FinancialActionJobManager
import co.nilin.mixchange.accountant.core.api.OrderManager
import co.nilin.mixchange.accountant.core.api.TradeManager
import co.nilin.mixchange.accountant.core.service.FinancialActionJobManagerImpl
import co.nilin.mixchange.accountant.core.service.OrderManagerImpl
import co.nilin.mixchange.accountant.core.service.TradeManagerImpl
import co.nilin.mixchange.accountant.core.spi.*
import co.nilin.mixchange.matching.core.eventh.events.*
import co.nilin.mixchange.port.accountant.kafka.consumer.OrderKafkaListener
import co.nilin.mixchange.port.accountant.kafka.spi.OrderSubmitRequestListener
import co.nilin.mixchange.port.order.kafka.inout.OrderSubmitRequest
import co.nilin.mixchange.port.trade.consumer.EventKafkaListener
import co.nilin.mixchange.port.trade.consumer.TempEventKafkaListener
import co.nilin.mixchange.port.trade.consumer.TradeKafkaListener
import co.nilin.mixchange.port.trade.spi.EventListener
import co.nilin.mixchange.port.trade.spi.TempEventListener
import co.nilin.mixchange.port.trade.spi.TradeListener
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import java.lang.IllegalArgumentException

@Configuration
@EnableScheduling
class AppConfig {
    @Bean
    fun getFinancialActionJobManager(
        financialActionLoader: FinancialActionLoader,
        financialActionPersister: FinancialActionPersister,
        walletProxy: WalletProxy
    ): FinancialActionJobManager {
        return FinancialActionJobManagerImpl(financialActionLoader, financialActionPersister, walletProxy)
    }

    @Bean
    fun orderManager(
        pairConfigLoader: PairConfigLoader,
        financialActionPersister: FinancialActionPersister,
        financeActionLoader: FinancialActionLoader,
        orderPersister: OrderPersister,
        tempEventPersister: TempEventPersister,
        tempEventRepublisher: TempEventRepublisher
    ): OrderManager {
        return OrderManagerImpl(
            pairConfigLoader,
            financialActionPersister,
            financeActionLoader,
            orderPersister,
            tempEventPersister,
            tempEventRepublisher
        )
    }

    @Bean
    fun tradeManager(
        pairStaticRateLoader: PairStaticRateLoader,
        financeActionPersister: FinancialActionPersister,
        financeActionLoader: FinancialActionLoader,
        orderPersister: OrderPersister,
        tempEventPersister: TempEventPersister,
        walletProxy: WalletProxy,
        @Value("\${app.coin}") platformCoin: String,
        @Value("\${app.address}") platformAddress: String
    ): TradeManager {
        return TradeManagerImpl(
            pairStaticRateLoader,
            financeActionPersister,
            financeActionLoader,
            orderPersister,
            tempEventPersister,
            walletProxy,
            platformCoin,
            platformAddress
        )
    }

    @Bean
    fun orderListener(orderManager: OrderManager): OrderListener {
        return OrderListener(orderManager)
    }

    @Bean
    fun accountantTradeListener(tradeManager: TradeManager): AccountantTradeListener {
        return AccountantTradeListener(tradeManager)
    }

    @Bean
    fun accountantEventListener(
        orderManager: OrderManager
    ): AccountantEventListener {
        return AccountantEventListener(orderManager)
    }

    @Bean
    fun accountantTempEventListener(
        orderManager: OrderManager,
        tradeManager: TradeManager
    ): AccountantTempEventListener {
        return AccountantTempEventListener(orderManager, tradeManager)
    }

    @Bean
    fun orderKafkaListener(): OrderKafkaListener {
        return OrderKafkaListener()
    }

    @Autowired
    fun configureOrderListener(orderKafkaListener: OrderKafkaListener, orderListener: OrderListener) {
        orderKafkaListener.addOrderListener(orderListener)
    }

    @Autowired
    fun configureTradeListener(
        tradeKafkaListener: TradeKafkaListener,
        accountantTradeListener: AccountantTradeListener
    ) {
        tradeKafkaListener.addTradeListener(accountantTradeListener)
    }

    @Autowired
    fun configureEventListener(
        eventKafkaListener: EventKafkaListener,
        accountantEventListener: AccountantEventListener
    ) {
        eventKafkaListener.addEventListener(accountantEventListener)
    }

    @Autowired
    fun configureTempEventListener(
        tempEventKafkaListener: TempEventKafkaListener,
        accountantTempEventListener: AccountantTempEventListener
    ) {
        tempEventKafkaListener.addEventListener(accountantTempEventListener)
    }

    class OrderListener(val orderManager: OrderManager) : OrderSubmitRequestListener {

        override fun id(): String {
            return "OrderListener"
        }

        override fun onOrder(order: OrderSubmitRequest, partition: Int, offset: Long, timestamp: Long) {
            runBlocking(AppDispatchers.kafkaExecutor) {
                orderManager.handleRequestOrder(
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
    }

    class AccountantTradeListener(val tradeManager: TradeManager) : TradeListener {

        override fun id(): String {
            return "TradeListener"
        }

        override fun onTrade(tradeEvent: TradeEvent, partition: Int, offset: Long, timestamp: Long) {
            runBlocking(AppDispatchers.kafkaExecutor) {
                tradeManager.handleTrade(tradeEvent)
            }
        }
    }

    class AccountantEventListener(
        val orderManager: OrderManager
    ) : EventListener {

        override fun id(): String {
            return "EventListener"
        }

        override fun onEvent(coreEvent: CoreEvent, partition: Int, offset: Long, timestamp: Long) {
            runBlocking(AppDispatchers.kafkaExecutor) {
                if (coreEvent is CreateOrderEvent)
                    orderManager.handleNewOrder(coreEvent)
                else if (coreEvent is RejectOrderEvent)
                    orderManager.handleRejectOrder(coreEvent)
                else if (coreEvent is UpdatedOrderEvent)
                    orderManager.handleUpdateOrder(coreEvent)
                else if (coreEvent is CancelOrderEvent)
                    orderManager.handleCancelOrder(coreEvent)
                else {
                    throw IllegalArgumentException("Event is not accepted ${coreEvent::class.java}")
                }
            }
            println("onEvent")
        }
    }

    class AccountantTempEventListener(
        val orderManager: OrderManager,
        val tradeManager: TradeManager
    ) : TempEventListener {

        override fun id(): String {
            return "TempEventListener"
        }

        override fun onEvent(coreEvent: CoreEvent, partition: Int, offset: Long, timestamp: Long) {
            println("TempEvent " + coreEvent)
            runBlocking(AppDispatchers.kafkaExecutor) {
                if (coreEvent is CreateOrderEvent)
                    orderManager.handleNewOrder(coreEvent)
                else if (coreEvent is RejectOrderEvent)
                    orderManager.handleRejectOrder(coreEvent)
                else if (coreEvent is UpdatedOrderEvent)
                    orderManager.handleUpdateOrder(coreEvent)
                else if (coreEvent is CancelOrderEvent)
                    orderManager.handleCancelOrder(coreEvent)
                else if (coreEvent is TradeEvent)
                    tradeManager.handleTrade(coreEvent)
                else {
                    throw IllegalArgumentException("Event is not accepted ${coreEvent::class.java}")
                }
            }
            println("onEvent")
        }
    }
}