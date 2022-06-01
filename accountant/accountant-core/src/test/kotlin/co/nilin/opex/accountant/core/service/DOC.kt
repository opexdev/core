package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import java.time.LocalDateTime

object DOC {

    val fa = FinancialAction(
        null,
        TradeEvent::class.java.name,
        "trade_id",
        "BTC_USDT",
        10000.0.toBigDecimal(),
        "user_parent",
        "main",
        "system",
        "main",
        LocalDateTime.now()
    )

}