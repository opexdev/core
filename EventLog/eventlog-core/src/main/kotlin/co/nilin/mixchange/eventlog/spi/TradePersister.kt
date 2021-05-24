package co.nilin.mixchange.eventlog.spi

import co.nilin.mixchange.matching.core.eventh.events.TradeEvent

interface TradePersister {
    suspend fun saveTrade(tradeEvent: TradeEvent): Trade
}