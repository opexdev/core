package co.nilin.opex.port.eventlog.kafka.spi

import co.nilin.opex.matching.core.eventh.events.TradeEvent

interface TradeListener {
    fun id(): String
    fun onTrade(tradeEvent: TradeEvent, partition: Int, offset: Long, timestamp: Long)
}