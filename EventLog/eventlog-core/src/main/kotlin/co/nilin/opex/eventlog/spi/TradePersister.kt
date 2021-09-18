package co.nilin.opex.eventlog.spi

import co.nilin.opex.matching.core.eventh.events.TradeEvent

interface TradePersister {
    suspend fun saveTrade(tradeEvent: TradeEvent): Trade
}