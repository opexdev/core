package co.nilin.opex.eventlog.core.spi

import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent

interface TradePersister {
    suspend fun saveTrade(tradeEvent: TradeEvent): Trade
}