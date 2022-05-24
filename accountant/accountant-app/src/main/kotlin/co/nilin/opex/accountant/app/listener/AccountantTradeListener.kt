package co.nilin.opex.accountant.app.listener

import co.nilin.opex.accountant.core.api.TradeManager
import co.nilin.opex.accountant.ports.kafka.listener.spi.TradeListener
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import kotlinx.coroutines.runBlocking

class AccountantTradeListener(private val tradeManager: TradeManager) : TradeListener {

    override fun id(): String {
        return "TradeListener"
    }

    override fun onTrade(tradeEvent: TradeEvent, partition: Int, offset: Long, timestamp: Long) {
        runBlocking {
            tradeManager.handleTrade(tradeEvent)
        }
    }
}