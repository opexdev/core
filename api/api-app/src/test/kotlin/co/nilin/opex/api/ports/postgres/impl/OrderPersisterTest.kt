package co.nilin.opex.api.ports.postgres.impl

import co.nilin.opex.api.ports.postgres.dao.OrderRepository
import co.nilin.opex.api.ports.postgres.dao.OrderStatusRepository
import co.nilin.opex.api.ports.postgres.impl.sample.Valid
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing
import reactor.core.publisher.Mono

class OrderPersisterTest {
    private val orderRepository: OrderRepository = mock()
    private val orderStatusRepository: OrderStatusRepository = mock()
    private val orderPersister = OrderPersisterImpl(orderRepository, orderStatusRepository)

    @Test
    fun givenRichOrder_whenSave_thenSuccess(): Unit = runBlocking {
        stubbing(orderRepository) {
            on {
                save(any())
            } doReturn Mono.just(Valid.MAKER_ORDER_MODEL)
        }
        stubbing(orderStatusRepository) {
            on {
                save(any())
            } doReturn Mono.just(Valid.MAKER_ORDER_STATUS_MODEL)
        }

        assertThatNoException().isThrownBy { runBlocking { orderPersister.save(Valid.RICH_ORDER) } }
    }

    @Test
    fun givenRichOrder_whenUpdate_thenSuccess(): Unit = runBlocking {
        stubbing(orderStatusRepository) {
            on {
                save(any())
            } doReturn Mono.just(Valid.MAKER_ORDER_STATUS_MODEL)
        }

        assertThatNoException().isThrownBy { runBlocking { orderPersister.update(Valid.RICH_ORDER_UPDATE) } }
    }
}
