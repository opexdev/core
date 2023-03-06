package co.nilin.opex.accountant.app.listener

import co.nilin.opex.accountant.core.api.FinancialActionProcessor
import co.nilin.opex.accountant.core.api.TradeManager
import co.nilin.opex.accountant.ports.kafka.listener.spi.Listener
import co.nilin.opex.accountant.ports.kafka.listener.spi.TradeListener
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import kotlinx.coroutines.runBlocking

class AccountantTradeListener(
    private val tradeManager: TradeManager,
    private val financialActionProcessor: FinancialActionProcessor
) : TradeListener {

    override fun id(): String {
        return "TradeListener"
    }

    override fun onEvent(event: TradeEvent, partition: Int, offset: Long, timestamp: Long) {
        runBlocking {
            val fa = tradeManager.handleTrade(event)
            financialActionProcessor.process(fa)
        }
    }
}