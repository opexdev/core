package co.nilin.opex.websocket.app.config

import co.nilin.opex.websocket.ports.kafka.listener.consumer.OrderKafkaListener
import co.nilin.opex.websocket.ports.kafka.listener.consumer.TradeKafkaListener
import co.nilin.opex.port.websocket.app.listener.WebSocketKafkaListener
import co.nilin.opex.websocket.core.spi.EventStreamHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {

    @Autowired
    fun configureListeners(
        orderKafkaListener: OrderKafkaListener,
        tradeKafkaListener: TradeKafkaListener,
        appListener: WebSocketKafkaListener
    ) {
        orderKafkaListener.addOrderListener(appListener)
        tradeKafkaListener.addTradeListener(appListener)
    }

    @Bean
    fun websocketListener(eventStreamHandler: EventStreamHandler): WebSocketKafkaListener {
        return WebSocketKafkaListener(eventStreamHandler)
    }

}