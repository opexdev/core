package co.nilin.opex.port.accountant.kafka.consumer

import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import co.nilin.opex.port.accountant.kafka.spi.TradeListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

@Component
class TradeKafkaListener : MessageListener<String, TradeEvent> {

    val tradeListeners = arrayListOf<TradeListener>()

    override fun onMessage(data: ConsumerRecord<String, TradeEvent>) {
        tradeListeners.forEach { tl ->
            tl.onTrade(data.value(), data.partition(), data.offset(), data.timestamp())
        }
    }

    fun addTradeListener(tl: TradeListener) {
        tradeListeners.add(tl)
    }

    fun removeTradeListener(tl: TradeListener) {
        tradeListeners.removeIf { item ->
            item.id() == tl.id()
        }
    }
}