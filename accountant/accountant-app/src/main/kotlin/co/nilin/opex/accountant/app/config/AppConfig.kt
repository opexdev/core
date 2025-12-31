package co.nilin.opex.accountant.app.config

import co.nilin.opex.accountant.app.listener.*
import co.nilin.opex.accountant.core.api.FeeCalculator
import co.nilin.opex.accountant.core.api.FinancialActionJobManager
import co.nilin.opex.accountant.core.api.OrderManager
import co.nilin.opex.accountant.core.api.TradeManager
import co.nilin.opex.accountant.core.service.FinancialActionJobManagerImpl
import co.nilin.opex.accountant.core.service.OrderManagerImpl
import co.nilin.opex.accountant.core.service.TradeManagerImpl
import co.nilin.opex.accountant.core.spi.*
import co.nilin.opex.accountant.ports.kafka.listener.consumer.*
import co.nilin.opex.accountant.ports.kafka.listener.spi.FAResponseListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
class AppConfig(
    @Value("\${app.trade-volume-calculation-currency}")
    private val tradeVolumeCalculationCurrency: String,
    @Value("\${app.zone-offset}")
    private val zoneOffsetString: String,
) {

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
        financialActionPersister: FinancialActionPersister,
        financeActionLoader: FinancialActionLoader,
        orderPersister: OrderPersister,
        tempEventPersister: TempEventPersister,
        tempEventRepublisher: TempEventRepublisher,
        richOrderPublisher: RichOrderPublisher,
        financialActionPublisher: FinancialActionPublisher,
        feeCalculator: FeeCalculator
    ): OrderManager {
        return OrderManagerImpl(
            pairConfigLoader,
            financialActionPersister,
            financeActionLoader,
            orderPersister,
            tempEventPersister,
            richOrderPublisher,
            financialActionPublisher,
            feeCalculator
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
        currencyRatePersister: CurrencyRatePersister,
        userVolumePersister: UserVolumePersister
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
            currencyRatePersister,
            userVolumePersister,
            tradeVolumeCalculationCurrency,
            zoneOffsetString
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

    @Bean
    fun faResponseListener(financialActionPersister: FinancialActionPersister): FAResponseListener {
        return AccountantFAResponseEventListener(financialActionPersister)
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

    @Autowired
    fun configureWithdrawRequestEventListener(
        withdrawRequestKafkaListener: WithdrawRequestKafkaListener,
        withdrawRequestEventListener: WithdrawRequestEventListener
    ) {
        withdrawRequestKafkaListener.addListener(withdrawRequestEventListener)
    }


    @Autowired
    fun configureDepositEventListener(
        depositKafkaListener: DepositKafkaListener,
        depositEventListener: DepositEventListener
    ) {
        depositKafkaListener.addListener(depositEventListener)
    }
}