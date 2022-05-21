package co.nilin.opex.api.ports.postgres.impl

import co.nilin.opex.api.core.event.RichOrder
import co.nilin.opex.api.core.event.RichOrderUpdate
import co.nilin.opex.api.core.inout.MatchConstraint
import co.nilin.opex.api.core.inout.MatchingOrderType
import co.nilin.opex.api.core.inout.OrderDirection
import co.nilin.opex.api.core.inout.OrderStatus
import co.nilin.opex.api.ports.postgres.dao.OrderRepository
import co.nilin.opex.api.ports.postgres.dao.OrderStatusRepository
import co.nilin.opex.api.ports.postgres.model.OrderModel
import co.nilin.opex.api.ports.postgres.model.OrderStatusModel
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset

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
        stubbing(orderRepository) {
            on {
                save(any())
            } doReturn Mono.just(
                OrderModel(
                    1,
                    "f1167d30-ccc0-4f86-ab5d-dd24aa3250df",
                    "18013d13-0568-496b-b93b-2524c8132b93",
                    "id", // ?
                    "ETH_USDT",
                    1,
                    0.01,
                    0.01,
                    0.0001,
                    0.01,
                    "1",
                    OrderDirection.ASK,
                    MatchConstraint.GTC,
                    MatchingOrderType.LIMIT_ORDER,
                    100000.0,
                    0.01,
                    0.0, // ?
                    LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC),
                    LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC)
                )
            )
        }
        stubbing(orderStatusRepository) {
            on {
                save(any())
            } doReturn Mono.just(
                OrderStatusModel(
                    "f1167d30-ccc0-4f86-ab5d-dd24aa3250df",
                    0.0, // ?
                    0.0, // ?
                    0, // ?
                    0, // ?
                    LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC)
                )
            )
        }

        assertThatNoException().isThrownBy { runBlocking { orderPersister.save(richOrder) } }
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
        stubbing(orderStatusRepository) {
            on {
                save(any())
            } doReturn Mono.just(
                OrderStatusModel(
                    "f1167d30-ccc0-4f86-ab5d-dd24aa3250df",
                    0.0, // ?
                    0.0, // ?
                    0, // ?
                    0, // ?
                    LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC)
                )
            )
        }

        assertThatNoException().isThrownBy { runBlocking { orderPersister.update(richOrderUpdate) } }
    }
}
