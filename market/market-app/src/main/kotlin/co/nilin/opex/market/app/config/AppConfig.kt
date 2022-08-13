package co.nilin.opex.market.app.config

import co.nilin.opex.market.app.listener.MarketListenerImpl
import co.nilin.opex.market.core.spi.OrderPersister
import co.nilin.opex.market.core.spi.TradePersister
import co.nilin.opex.market.ports.kafka.listener.consumer.OrderKafkaListener
import co.nilin.opex.market.ports.kafka.listener.consumer.TradeKafkaListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {

    @Bean
    fun marketListener(richOrderPersister: OrderPersister, richTradePersister: TradePersister): MarketListenerImpl {
        return MarketListenerImpl(richOrderPersister, richTradePersister)
    }

    @Autowired
    fun configureListeners(
        orderKafkaListener: OrderKafkaListener,
        tradeKafkaListener: TradeKafkaListener,
        appListener: MarketListenerImpl
    ) {
        orderKafkaListener.addOrderListener(appListener)
        tradeKafkaListener.addTradeListener(appListener)
    }

}