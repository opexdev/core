package co.nilin.opex.eventlog.ports.kafka.listener.spi

import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent

interface TradeListener {
    fun id(): String
    fun onTrade(tradeEvent: TradeEvent, partition: Int, offset: Long, timestamp: Long)
}