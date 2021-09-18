package co.nilin.opex.port.api.kafka.spi

import co.nilin.opex.accountant.core.inout.RichTrade

interface RichTradeListener {
    fun id(): String
    fun onTrade(trade: RichTrade, partition: Int, offset: Long, timestamp: Long)
}