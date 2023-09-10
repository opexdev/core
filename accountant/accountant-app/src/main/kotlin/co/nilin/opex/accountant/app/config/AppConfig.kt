package co.nilin.opex.accountant.app.config

import co.nilin.opex.accountant.app.listener.*
import co.nilin.opex.accountant.core.api.*
import co.nilin.opex.accountant.core.service.FinancialActionJobManagerImpl
import co.nilin.opex.accountant.core.service.OrderManagerImpl
import co.nilin.opex.accountant.core.service.TradeManagerImpl
import co.nilin.opex.accountant.core.spi.*
import co.nilin.opex.accountant.ports.kafka.listener.consumer.*
import co.nilin.opex.accountant.ports.postgres.impl.UserLevelLoaderImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
class AppConfig {

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
        financialActionPublisher: FinancialActionPublisher,
    ): OrderManager {
        return OrderManagerImpl(
            pairConfigLoader,
            userLevelLoader,
            financialActionPersister,
            financeActionLoader,
            orderPersister,
            tempEventPersister,
            richOrderPublisher,
            financialActionPublisher
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
        financialActionPublisher: FinancialActionPublisher,
    ): TradeManager {
        return TradeManagerImpl(
            financeActionPersister,
            financeActionLoader,
            orderPersister,
            tempEventPersister,
            richTradePublisher,
            richOrderPublisher,
            feeCalculator,
            financialActionPublisher
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
            accountantEventListener: AccountantEventListener,
            kycLevelUpdatedKafkaListener: KycLevelUpdatedKafkaListener,
            kycLevelUpdatedEventListener: KycLevelUpdatedListener
    ) {
        eventKafkaListener.addListener(accountantEventListener)
        kycLevelUpdatedKafkaListener.addEventListener(kycLevelUpdatedEventListener)

    }

    @Autowired
    fun configureTempEventListener(
        tempEventKafkaListener: TempEventKafkaListener,
        accountantTempEventListener: AccountantTempEventListener,

    ) {
        tempEventKafkaListener.addListener(accountantTempEventListener)
    }

}