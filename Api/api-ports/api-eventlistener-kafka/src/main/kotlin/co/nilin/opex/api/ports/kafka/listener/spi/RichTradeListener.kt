package co.nilin.opex.api.ports.kafka.listener.spi

import co.nilin.opex.accountant.core.inout.RichTrade

interface RichTradeListener {
    fun id(): String
    fun onTrade(trade: RichTrade, partition: Int, offset: Long, timestamp: Long)
}