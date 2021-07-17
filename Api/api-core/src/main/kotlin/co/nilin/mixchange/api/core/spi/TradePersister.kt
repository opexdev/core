package co.nilin.mixchange.api.core.spi

import co.nilin.mixchange.accountant.core.inout.RichTrade

interface TradePersister {
    suspend fun save(trade: RichTrade)
}