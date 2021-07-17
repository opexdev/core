package co.nilin.mixchange.port.trade.spi

import co.nilin.mixchange.accountant.core.inout.RichTrade

interface RichTradeListener {
    fun id(): String
    fun onTrade(trade: RichTrade, partition: Int, offset: Long, timestamp: Long)
}