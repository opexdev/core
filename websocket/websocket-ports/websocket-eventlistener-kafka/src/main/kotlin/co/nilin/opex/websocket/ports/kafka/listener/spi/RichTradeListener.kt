package co.nilin.opex.websocket.ports.kafka.listener.spi

import co.nilin.opex.websocket.core.inout.RichTrade

interface RichTradeListener {

    fun id(): String

    fun onTrade(trade: RichTrade, partition: Int, offset: Long, timestamp: Long)
}