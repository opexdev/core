package co.nilin.opex.accountant.ports.kafka.submitter

import co.nilin.opex.accountant.core.inout.RichOrderEvent
import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.Pair
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.SettableListenableFuture
import java.time.LocalDateTime

object Valid {

    class TestRichOrderEvent : RichOrderEvent
    class TestCoreEvent : CoreEvent(Pair("BTC", "USDT"))

    val testRichOrder = TestRichOrderEvent()
    val testCoreEvent = TestCoreEvent()
    val richTrade = RichTrade(
        1,
        "BTC_USDT",
        "",
        "",
        1,
        OrderDirection.BID,
        1.0.toBigDecimal(),
        1.0.toBigDecimal(),
        1.0.toBigDecimal(),
        1.0.toBigDecimal(),
        1.0.toBigDecimal(),
        "",
        "",
        "",
        1,
        OrderDirection.BID,
        1.0.toBigDecimal(),
        1.0.toBigDecimal(),
        1.0.toBigDecimal(),
        1.0.toBigDecimal(),
        1.0.toBigDecimal(),
        "",
        1.0.toBigDecimal(),
        LocalDateTime.now()
    )

    fun <T> kafkaSendFuture() = SettableListenableFuture<SendResult<String, T>>().apply {
        set(SendResult(null, null))
    }

}