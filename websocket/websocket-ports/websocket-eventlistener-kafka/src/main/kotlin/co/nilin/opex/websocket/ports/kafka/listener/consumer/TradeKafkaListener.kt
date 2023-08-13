package co.nilin.opex.websocket.ports.kafka.listener.consumer

import co.nilin.opex.websocket.core.inout.RichTrade
import co.nilin.opex.websocket.ports.kafka.listener.spi.RichTradeListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

@Component
class TradeKafkaListener : MessageListener<String, RichTrade> {

    val tradeListeners = arrayListOf<RichTradeListener>()

    override fun onMessage(data: ConsumerRecord<String, RichTrade>) {
        tradeListeners.forEach { tl ->
            tl.onTrade(data.value(), data.partition(), data.offset(), data.timestamp())
        }
    }

    fun addTradeListener(tl: RichTradeListener) {
        tradeListeners.add(tl)
    }

    fun removeTradeListener(tl: RichTradeListener) {
        tradeListeners.removeIf { item ->
            item.id() == tl.id()
        }
    }
}