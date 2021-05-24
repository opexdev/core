package co.nilin.mixchange.port.trade.spi

import co.nilin.mixchange.matching.core.eventh.events.TradeEvent


interface TradeListener {
    fun id(): String
    fun onTrade(tradeEvent: TradeEvent, partition: Int, offset: Long, timestamp: Long)
}