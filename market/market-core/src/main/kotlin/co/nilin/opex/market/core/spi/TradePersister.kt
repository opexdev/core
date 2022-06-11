package co.nilin.opex.market.core.spi

import co.nilin.opex.market.core.event.RichTrade

interface TradePersister {
    suspend fun save(trade: RichTrade)
}