package co.nilin.opex.app.config

import co.nilin.opex.accountant.core.inout.RichOrder
import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.api.core.spi.OrderPersister
import co.nilin.opex.api.core.spi.TradePersister
import co.nilin.opex.port.api.kafka.consumer.OrderKafkaListener
import co.nilin.opex.port.api.kafka.spi.RichOrderListener
import co.nilin.opex.port.api.kafka.consumer.TradeKafkaListener
import co.nilin.opex.port.api.kafka.spi.RichTradeListener
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {

    @Bean
    fun apiListener(
        richOrderPersister: OrderPersister,
        richTradePersister: TradePersister
    ): ApiListenerImpl {
        return ApiListenerImpl(richOrderPersister, richTradePersister)
    }

    @Autowired
    fun configureListeners(
        orderKafkaListener: OrderKafkaListener,
        tradeKafkaListener: TradeKafkaListener,
        appListener: ApiListenerImpl
    ) {
        orderKafkaListener.addOrderListener(appListener)
        tradeKafkaListener.addTradeListener(appListener)
    }

    class ApiListenerImpl(
        val richOrderPersister: OrderPersister,
        val richTradePersister: TradePersister
    ) : RichTradeListener, RichOrderListener {

        override fun id(): String {
            return "AppListener"
        }

        override fun onTrade(
            trade: RichTrade,
            partition: Int,
            offset: Long,
            timestamp: Long
        ) {
            println("RichTrade received")
            runBlocking(AppDispatchers.kafkaExecutor) {
                richTradePersister.save(trade)
            }
        }

        override fun onOrder(
            order: RichOrder,
            partition: Int,
            offset: Long,
            timestamp: Long
        ) {
            runBlocking(AppDispatchers.kafkaExecutor) {
                richOrderPersister.save(order)
            }
        }
    }


}