package co.nilin.mixchange.accountant.core.spi

import co.nilin.mixchange.accountant.core.inout.RichTrade

interface RichTradePublisher {
    suspend fun publish(trade: RichTrade)
}