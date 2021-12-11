package co.nilin.opex.referral.ports.kafka.listener.spi

import co.nilin.opex.accountant.core.inout.RichTrade

interface RichTradeListener {
    fun id(): String
    fun onTrade(richTrade: RichTrade, partition: Int, offset: Long, timestamp: Long)
}