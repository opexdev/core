package co.nilin.opex.market.ports.kafka.listener.spi

import co.nilin.opex.market.core.event.RichTrade

interface RichTradeListener {

    fun id(): String

    fun onTrade(trade: RichTrade, partition: Int, offset: Long, timestamp: Long)
}