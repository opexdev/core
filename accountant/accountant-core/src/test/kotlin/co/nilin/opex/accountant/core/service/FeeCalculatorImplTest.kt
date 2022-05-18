package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.inout.OrderStatus
import co.nilin.opex.accountant.core.model.Order
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import co.nilin.opex.matching.engine.core.model.Pair
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class FeeCalculatorImplTest {

    private val feeCalculator = FeeCalculatorImpl("0x0")

    @Test
    fun test() = runBlocking {
        val maker = Order(
            "btc_usdt",
            "order_1",
            1,
            0.01,
            0.01,
            0.00001,
            0.01,
            "user_1",
            "*",
            OrderDirection.BID,
            MatchConstraint.GTC,
            OrderType.LIMIT_ORDER,
            50_000,
            1000,
            0,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            OrderStatus.NEW.code
        )

        val taker = Order(
            "btc_usdt",
            "order_2",
            2,
            0.01,
            0.01,
            0.00001,
            0.01,
            "user_2",
            "*",
            OrderDirection.ASK,
            MatchConstraint.GTC,
            OrderType.LIMIT_ORDER,
            50_000,
            1000,
            0,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            OrderStatus.NEW.code
        )
        val trade = TradeEvent(
            1,
            Pair("btc", "usdt"),
            "order_2",
            "user_2",
            2,
            OrderDirection.ASK,
            50_000,
            0,
            "order_1",
            "user_1",
            1,
            OrderDirection.BID,
            50_000,
            0,
            1000
        )
        val actions = feeCalculator.createFeeActions()
    }

}