package co.nilin.opex.accountant.core.spi

import co.nilin.opex.accountant.core.inout.RichTrade

interface RichTradePublisher {
    suspend fun publish(trade: RichTrade)
}