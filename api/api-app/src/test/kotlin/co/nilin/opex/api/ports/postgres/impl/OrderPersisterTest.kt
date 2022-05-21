package co.nilin.opex.api.ports.postgres.impl

import co.nilin.opex.api.core.event.RichOrder
import co.nilin.opex.api.core.event.RichOrderUpdate
import co.nilin.opex.api.core.inout.MatchConstraint
import co.nilin.opex.api.core.inout.MatchingOrderType
import co.nilin.opex.api.core.inout.OrderDirection
import co.nilin.opex.api.core.inout.OrderStatus
import co.nilin.opex.api.ports.postgres.dao.OrderRepository
import co.nilin.opex.api.ports.postgres.dao.OrderStatusRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.assertj.core.api.Assertions.*
import java.math.BigDecimal

class OrderPersisterTest {
    private val orderRepository: OrderRepository = mock()
    private val orderStatusRepository: OrderStatusRepository = mock()
    private val orderPersister = OrderPersisterImpl(orderRepository, orderStatusRepository)

    @Test
    fun givenRichOrder_whenSave_thenSuccess(): Unit = runBlocking {
        val richOrder = RichOrder(
            null,
            "ETH_USDT",
            "f1167d30-ccc0-4f86-ab5d-dd24aa3250df",
            "18013d13-0568-496b-b93b-2524c8132b93",
            "1",
            BigDecimal.valueOf(0.01),
            BigDecimal.valueOf(0.01),
            BigDecimal.valueOf(0.0001),
            BigDecimal.valueOf(0.01),
            OrderDirection.ASK,
            MatchConstraint.GTC,
            MatchingOrderType.LIMIT_ORDER,
            BigDecimal.valueOf(1000001),
            BigDecimal.valueOf(0.01),
            BigDecimal.valueOf(0), // ?
            BigDecimal.valueOf(0), // ?
            BigDecimal.valueOf(0), // ?
            0
        )

        assertThatThrownBy { runBlocking { orderPersister.save(richOrder) } }.doesNotThrowAnyException()
    }

    @Test
    fun givenRichOrder_whenUpdate_thenSuccess(): Unit = runBlocking {
        val richOrderUpdate = RichOrderUpdate(
            "f1167d30-ccc0-4f86-ab5d-dd24aa3250df",
            BigDecimal.valueOf(1000001),
            BigDecimal.valueOf(0.01),
            BigDecimal.valueOf(0.08),
            OrderStatus.PARTIALLY_FILLED
        )

        assertThatThrownBy { runBlocking { orderPersister.update(richOrderUpdate) } }.doesNotThrowAnyException()
    }
}
