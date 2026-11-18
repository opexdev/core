package co.nilin.opex.eventlog.app.config

import co.nilin.opex.eventlog.app.listeners.DeadLetterListener
import co.nilin.opex.eventlog.app.listeners.EventlogAccountingTransactionListener
import co.nilin.opex.eventlog.app.listeners.EventlogDepositListener
import co.nilin.opex.eventlog.app.listeners.EventlogEventListener
import co.nilin.opex.eventlog.app.listeners.EventlogPublishedEventListener
import co.nilin.opex.eventlog.app.listeners.EventlogSwapListener
import co.nilin.opex.eventlog.app.listeners.EventlogTradeListener
import co.nilin.opex.eventlog.app.listeners.EventlogWithdrawRequestListener
import co.nilin.opex.eventlog.app.listeners.OrderListener
import co.nilin.opex.eventlog.core.spi.DeadLetterPersister
import co.nilin.opex.eventlog.core.spi.EventPersister
import co.nilin.opex.eventlog.core.spi.OrderPersister
import co.nilin.opex.eventlog.core.spi.PublishedEventPersister
import co.nilin.opex.eventlog.core.spi.TradePersister
import co.nilin.opex.eventlog.core.accounting.AccountingTransactionLogWriter
import co.nilin.opex.eventlog.ports.kafka.listener.consumer.AccountingTransactionKafkaListener
import co.nilin.opex.eventlog.ports.kafka.listener.consumer.DLTKafkaListener
import co.nilin.opex.eventlog.ports.kafka.listener.consumer.DepositKafkaListener
import co.nilin.opex.eventlog.ports.kafka.listener.consumer.EventKafkaListener
import co.nilin.opex.eventlog.ports.kafka.listener.consumer.OrderKafkaListener
import co.nilin.opex.eventlog.ports.kafka.listener.consumer.PublishedEventKafkaListener
import co.nilin.opex.eventlog.ports.kafka.listener.consumer.SwapKafkaListener
import co.nilin.opex.eventlog.ports.kafka.listener.consumer.TradeKafkaListener
import co.nilin.opex.eventlog.ports.kafka.listener.consumer.WithdrawRequestKafkaListener
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
    fun eventlogPublishedEventListener(persister: PublishedEventPersister): EventlogPublishedEventListener {
        return EventlogPublishedEventListener(persister)
    }

    @Bean
    fun eventlogAccountingTransactionListener(logWriter: AccountingTransactionLogWriter): EventlogAccountingTransactionListener {
        return EventlogAccountingTransactionListener(logWriter)
    }

    @Bean
    fun eventlogDepositListener(logWriter: AccountingTransactionLogWriter): EventlogDepositListener {
        return EventlogDepositListener(logWriter)
    }

    @Bean
    fun eventlogWithdrawRequestListener(logWriter: AccountingTransactionLogWriter): EventlogWithdrawRequestListener {
        return EventlogWithdrawRequestListener(logWriter)
    }

    @Bean
    fun eventlogSwapListener(logWriter: AccountingTransactionLogWriter): EventlogSwapListener {
        return EventlogSwapListener(logWriter)
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

    @Autowired
    fun configurePublishedEventListener(
        publishedEventKafkaListener: PublishedEventKafkaListener,
        listener: EventlogPublishedEventListener
    ) {
        publishedEventKafkaListener.addListener(listener)
    }

    @Autowired
    fun configureAccountingTransactionListener(
        accountingTransactionKafkaListener: AccountingTransactionKafkaListener,
        listener: EventlogAccountingTransactionListener
    ) {
        accountingTransactionKafkaListener.addListener(listener)
    }

    @Autowired
    fun configureDepositListener(
        depositKafkaListener: DepositKafkaListener,
        listener: EventlogDepositListener
    ) {
        depositKafkaListener.addListener(listener)
    }

    @Autowired
    fun configureWithdrawRequestListener(
        withdrawRequestKafkaListener: WithdrawRequestKafkaListener,
        listener: EventlogWithdrawRequestListener
    ) {
        withdrawRequestKafkaListener.addListener(listener)
    }

    @Autowired
    fun configureSwapListener(
        swapKafkaListener: SwapKafkaListener,
        listener: EventlogSwapListener
    ) {
        swapKafkaListener.addListener(listener)
    }

}