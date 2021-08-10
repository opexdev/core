package co.nilin.opex.api.core.spi

import co.nilin.opex.accountant.core.inout.RichTrade

interface TradePersister {
    suspend fun save(trade: RichTrade)
}