package co.nilin.opex.market.ports.kafka.producer.producer

import co.nilin.opex.common.utils.LoggerDelegate
import co.nilin.opex.market.ports.kafka.producer.config.KafkaTopics
import co.nilin.opex.market.core.inout.MarketOrderEvent
import co.nilin.opex.market.core.spi.MarketOrderProducer
import co.nilin.opex.market.ports.kafka.producer.events.OpenOrderUpdateEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Component

@Component
class MarketOrderProducer(private val template: KafkaTemplate<String, MarketOrderEvent>) : MarketOrderProducer {

    private val logger by LoggerDelegate()

    private val retryTemplate = RetryTemplate.builder()
        .maxAttempts(10)
        .exponentialBackoff(1000, 1.8, 5 * 60 * 1000)
        .retryOn(Exception::class.java)
        .build()

    private suspend fun send(event: MarketOrderEvent) {
        retryTemplate.execute<Unit, Exception> {
            template.send(KafkaTopics.MARKET_ORDER, event).addCallback(
                { logger.info("Market order event sent") },
                { error -> logger.error("Error sending market order event", error) }
            )
        }
    }

    override suspend fun openOrderUpdate(uuid: String, pair: String) {
        send(OpenOrderUpdateEvent(uuid, pair))
    }
}