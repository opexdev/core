package co.nilin.opex.api.app.config

import co.nilin.opex.api.app.listener.ApiListenerImpl
import co.nilin.opex.api.core.spi.OrderPersister
import co.nilin.opex.api.core.spi.TradePersister
import co.nilin.opex.api.ports.kafka.listener.consumer.OrderKafkaListener
import co.nilin.opex.api.ports.kafka.listener.consumer.TradeKafkaListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {

    @Bean
    fun apiListener(richOrderPersister: OrderPersister, richTradePersister: TradePersister): ApiListenerImpl {
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

}