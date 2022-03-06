package co.nilin.opex.eventlog.app.listeners

import co.nilin.opex.eventlog.core.spi.TradePersister
import co.nilin.opex.eventlog.ports.kafka.listener.spi.TradeListener
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class EventlogTradeListener(private val tradePersister: TradePersister) : TradeListener {

    private val log = LoggerFactory.getLogger(EventlogTradeListener::class.java)

    override fun id(): String {
        return "TradeListener"
    }

    override fun onTrade(tradeEvent: TradeEvent, partition: Int, offset: Long, timestamp: Long) {
        log.debug("Receive TradeEvent {}", tradeEvent)
        runBlocking {
            tradePersister.saveTrade(tradeEvent)
        }
    }
}