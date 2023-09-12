package co.nilin.opex.accountant.app.config

import co.nilin.opex.accountant.app.listener.AccountantEventListener
import co.nilin.opex.accountant.app.listener.AccountantTempEventListener
import co.nilin.opex.accountant.app.listener.AccountantTradeListener
import co.nilin.opex.accountant.app.listener.OrderListener
import co.nilin.opex.accountant.core.api.FeeCalculator
import co.nilin.opex.accountant.core.api.FinancialActionJobManager
import co.nilin.opex.accountant.core.api.OrderManager
import co.nilin.opex.accountant.core.api.TradeManager
import co.nilin.opex.accountant.core.service.FinancialActionJobManagerImpl
import co.nilin.opex.accountant.core.service.OrderManagerImpl
import co.nilin.opex.accountant.core.service.TradeManagerImpl
import co.nilin.opex.accountant.core.spi.*
import co.nilin.opex.accountant.ports.kafka.listener.consumer.EventKafkaListener
import co.nilin.opex.accountant.ports.kafka.listener.consumer.OrderKafkaListener
import co.nilin.opex.accountant.ports.kafka.listener.consumer.TempEventKafkaListener
import co.nilin.opex.accountant.ports.kafka.listener.consumer.TradeKafkaListener
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
class AppConfig {

    @Bean
    fun getFinancialActionJobManager(
        financialActionLoader: FinancialActionLoader,
        financialActionPersister: FinancialActionPersister,
        walletProxy: WalletProxy
    ): FinancialActionJobManager {
        return FinancialActionJobManagerImpl(
            financialActionLoader,
            financialActionPersister,
            walletProxy
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
        financialActionPublisher: FinancialActionPublisher,
        jsonMapper: JsonMapper
    ): OrderManager {
        return OrderManagerImpl(
            pairConfigLoader,
            userLevelLoader,
            financialActionPersister,
            financeActionLoader,
            orderPersister,
            tempEventPersister,
            richOrderPublisher,
            financialActionPublisher,
            jsonMapper
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
        jsonMapper: JsonMapper
    ): TradeManager {
        return TradeManagerImpl(
            financeActionPersister,
            financeActionLoader,
            orderPersister,
            tempEventPersister,
            richTradePublisher,
            richOrderPublisher,
            feeCalculator,
            financialActionPublisher,
            jsonMapper
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