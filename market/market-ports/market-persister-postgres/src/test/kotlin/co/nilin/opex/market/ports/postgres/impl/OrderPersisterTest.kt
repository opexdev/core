package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.market.ports.postgres.dao.OpenOrderRepository
import co.nilin.opex.market.ports.postgres.dao.OrderRepository
import co.nilin.opex.market.ports.postgres.dao.OrderStatusRepository
import co.nilin.opex.market.ports.postgres.impl.sample.VALID
import co.nilin.opex.market.ports.postgres.util.RedisCacheHelper
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

class OrderPersisterTest {
    private val orderRepository = mockk<OrderRepository>()
    private val orderStatusRepository = mockk<OrderStatusRepository>()
    private val openOrderRepository = mockk<OpenOrderRepository>()
    private val redisCacheHelper = mockk<RedisCacheHelper>()
    private val orderPersister =
        OrderPersisterImpl(orderRepository, orderStatusRepository, openOrderRepository, redisCacheHelper)

    @Test
    fun givenOrderRepo_whenSaveRichOrder_thenSuccess(): Unit = runBlocking {
        every {
            orderRepository.save(any())
        } returns Mono.just(VALID.MAKER_ORDER_MODEL)
        every {
            orderStatusRepository.insert(any(), any(), any(), any(), any(), any())
        } returns Mono.empty()
        every {
            orderStatusRepository.findMostRecentByOUID(any())
        } returns Mono.just(VALID.MAKER_ORDER_STATUS_MODEL)
        every {
            openOrderRepository.insertOrUpdate(any(), any(), any())
        } returns Mono.empty()
        every {
            openOrderRepository.delete(any<String>())
        } returns Mono.empty()
        every { redisCacheHelper.put(any(), any()) } returns Unit

        assertThatNoException().isThrownBy { runBlocking { orderPersister.save(VALID.RICH_ORDER) } }
    }

    @Test
    fun givenOrderRepo_whenUpdateRichOrder_thenSuccess(): Unit = runBlocking {
        every {
            orderStatusRepository.insert(any(), any(), any(), any(), any(), any())
        } returns Mono.empty()
        every {
            orderStatusRepository.findMostRecentByOUID(any())
        } returns Mono.just(VALID.MAKER_ORDER_STATUS_MODEL)
        every {
            openOrderRepository.insertOrUpdate(any(), any(), any())
        } returns Mono.empty()
        every {
            openOrderRepository.delete(any<String>())
        } returns Mono.empty()

        assertThatNoException().isThrownBy { runBlocking { orderPersister.update(VALID.RICH_ORDER_UPDATE) } }
    }
}
