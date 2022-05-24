package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.event.RichTrade

interface TradePersister {
    suspend fun save(trade: RichTrade)
}