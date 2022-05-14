package co.nilin.opex.api.ports.kafka.listener.spi

import co.nilin.opex.api.core.event.RichTrade

interface RichTradeListener {
    fun id(): String
    fun onTrade(trade: RichTrade, partition: Int, offset: Long, timestamp: Long)
}