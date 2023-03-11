package co.nilin.opex.accountant.app.config

import co.nilin.opex.accountant.app.listener.AccountantEventListener
import co.nilin.opex.accountant.app.listener.AccountantTempEventListener
import co.nilin.opex.accountant.app.listener.AccountantTradeListener
import co.nilin.opex.accountant.app.listener.OrderListener
import co.nilin.opex.accountant.core.api.FeeCalculator
import co.nilin.opex.accountant.core.api.FinancialActionProcessor
import co.nilin.opex.accountant.core.api.OrderManager
import co.nilin.opex.accountant.core.api.TradeManager
import co.nilin.opex.accountant.core.service.FinancialActionProcessorImpl
import co.nilin.opex.accountant.core.service.OrderManagerImpl
import co.nilin.opex.accountant.core.service.TradeManagerImpl
import co.nilin.opex.accountant.core.spi.*
import co.nilin.opex.accountant.ports.kafka.listener.consumer.EventKafkaListener
import co.nilin.opex.accountant.ports.kafka.listener.consumer.OrderKafkaListener
import co.nilin.opex.accountant.ports.kafka.listener.consumer.TempEventKafkaListener
import co.nilin.opex.accountant.ports.kafka.listener.consumer.TradeKafkaListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
class AppConfig {

    @Bean
    fun financialActionProcessor(
        financialActionLoader: FinancialActionLoader,
        financialActionPersister: FinancialActionPersister,
        financialActionPublisher: FinancialActionPublisher
    ): FinancialActionProcessor {
        return FinancialActionProcessorImpl(
            financialActionLoader,
            financialActionPersister,
            financialActionPublisher
        )
    }

    @Bean
    fun orderManager(
        pairConfigLoader: PairConfigLoader,
        userLevelLoader: UserLevelLoader,
        financialActionPersister: FinancialActionPersister,
        financeActionLoader: FinancialActionLoader,
        orderPersister: OrderPersister,
        tempEventPersister: TempEventPersister,
        tempEventRepublisher: TempEventRepublisher,
        richOrderPublisher: RichOrderPublisher,
        financialActionProcessor: FinancialActionProcessor
    ): OrderManager {
        return OrderManagerImpl(
            pairConfigLoader,
            userLevelLoader,
            financialActionPersister,
            financeActionLoader,
            orderPersister,
            tempEventPersister,
            richOrderPublisher,
            financialActionProcessor
        )
    }

    @Bean
    fun tradeManager(
        financeActionPersister: FinancialActionPersister,
        financeActionLoader: FinancialActionLoader,
        orderPersister: OrderPersister,
        tempEventPersister: TempEventPersister,
        richTradePublisher: RichTradePublisher,
        richOrderPublisher: RichOrderPublisher,
        feeCalculator: FeeCalculator,
        financialActionProcessor: FinancialActionProcessor
    ): TradeManager {
        return TradeManagerImpl(
            financeActionPersister,
            financeActionLoader,
            orderPersister,
            tempEventPersister,
            richTradePublisher,
            richOrderPublisher,
            feeCalculator,
            financialActionProcessor
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
    fun accountantEventListener(orderManager: OrderManager): AccountantEventListener {
        return AccountantEventListener(orderManager)
    }

    @Bean
    fun accountantTempEventListener(
        orderManager: OrderManager,
        tradeManager: TradeManager
    ): AccountantTempEventListener {
        return AccountantTempEventListener(orderManager, tradeManager)
    }

    @Autowired
    fun configureOrderListener(orderKafkaListener: OrderKafkaListener, orderListener: OrderListener) {
        orderKafkaListener.addListener(orderListener)
    }

    @Autowired
    fun configureTradeListener(
        tradeKafkaListener: TradeKafkaListener,
        accountantTradeListener: AccountantTradeListener
    ) {
        tradeKafkaListener.addListener(accountantTradeListener)
    }

    @Autowired
    fun configureEventListener(
        eventKafkaListener: EventKafkaListener,
        accountantEventListener: AccountantEventListener
    ) {
        eventKafkaListener.addListener(accountantEventListener)
    }

    @Autowired
    fun configureTempEventListener(
        tempEventKafkaListener: TempEventKafkaListener,
        accountantTempEventListener: AccountantTempEventListener
    ) {
        tempEventKafkaListener.addListener(accountantTempEventListener)
    }

}