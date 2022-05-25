package co.nilin.opex.api.ports.postgres.impl

import co.nilin.opex.api.ports.postgres.dao.OrderRepository
import co.nilin.opex.api.ports.postgres.dao.OrderStatusRepository
import co.nilin.opex.api.ports.postgres.impl.sample.VALID
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

class OrderPersisterTest {
    private val orderRepository: OrderRepository = mockk()
    private val orderStatusRepository: OrderStatusRepository = mockk()
    private val orderPersister = OrderPersisterImpl(orderRepository, orderStatusRepository)

    @Test
    fun givenOrderRepo_whenSaveRichOrder_thenSuccess(): Unit = runBlocking {
        every {
            orderRepository.save(any())
        } returns Mono.just(VALID.MAKER_ORDER_MODEL)
        every {
            orderStatusRepository.save(any())
        } returns Mono.just(VALID.MAKER_ORDER_STATUS_MODEL)

        assertThatNoException().isThrownBy { runBlocking { orderPersister.save(VALID.RICH_ORDER) } }
    }

    @Test
    fun givenOrderRepo_whenUpdateRichOrder_thenSuccess(): Unit = runBlocking {
        every {
            orderStatusRepository.save(any())
        } returns Mono.just(VALID.MAKER_ORDER_STATUS_MODEL)

        assertThatNoException().isThrownBy { runBlocking { orderPersister.update(VALID.RICH_ORDER_UPDATE) } }
    }
}
