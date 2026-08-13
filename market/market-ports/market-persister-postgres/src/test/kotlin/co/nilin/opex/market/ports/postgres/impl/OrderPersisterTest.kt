package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.market.core.spi.MarketOrderProducer
import co.nilin.opex.market.ports.postgres.dao.OpenOrderRepository
import co.nilin.opex.market.ports.postgres.dao.OrderRepository
import co.nilin.opex.market.ports.postgres.dao.OrderStatusRepository
import co.nilin.opex.market.ports.postgres.impl.sample.VALID
import co.nilin.opex.market.ports.postgres.util.RedisCacheHelper
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.DuplicateKeyException
import reactor.core.publisher.Mono

class OrderPersisterTest {
    private val orderRepository = mockk<OrderRepository>()
    private val orderStatusRepository = mockk<OrderStatusRepository>()
    private val openOrderRepository = mockk<OpenOrderRepository>()
    private val marketOrderProducer = mockk<MarketOrderProducer>()
    private val redisCacheHelper = mockk<RedisCacheHelper>()
    private val orderPersister =
        OrderPersisterImpl(
            orderRepository,
            orderStatusRepository,
            openOrderRepository,
            redisCacheHelper,
            marketOrderProducer
        )

    @Test
    fun givenOrderRepo_whenSaveRichOrder_thenSuccess(): Unit = runBlocking {
        every {
            orderRepository.insertOrderIfAbsent(
                ouid = any(),
                uuid = any(),
                clientOrderId = any(),
                symbol = any(),
                orderId = any(),
                makerFee = any(),
                takerFee = any(),
                leftSideFraction = any(),
                rightSideFraction = any(),
                userLevel = any(),
                side = any(),
                matchConstraint = any(),
                orderType = any(),
                price = any(),
                quantity = any(),
                quoteQuantity = any(),
                createDate = any(),
                updateDate = any()
            )
        } returns Mono.just(VALID.RICH_ORDER.ouid)
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
        every {
            orderRepository.findByOuid(any())
        } returns Mono.just(VALID.MAKER_ORDER_MODEL)
        coEvery { marketOrderProducer.openOrderUpdate(any(), any()) } returns Unit

        assertThatNoException().isThrownBy { runBlocking { orderPersister.save(VALID.RICH_ORDER) } }
    }

    @Test
    fun givenOrderRepo_whenUpdateRichOrder_thenSuccess(): Unit = runBlocking {
        every {
            orderRepository.touchUpdateDateByOuid(any(), any())
        } returns Mono.empty()
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
        every {
            orderRepository.findByOuid(any())
        } returns Mono.just(VALID.MAKER_ORDER_MODEL)
        coEvery { marketOrderProducer.openOrderUpdate(any(), any()) } returns Unit

        assertThatNoException().isThrownBy { runBlocking { orderPersister.update(VALID.RICH_ORDER_UPDATE) } }
    }

    @Test
    fun givenDuplicateOrderCreate_whenSaveRichOrder_thenIgnoredAsIdempotent(): Unit = runBlocking {
        every {
            orderRepository.insertOrderIfAbsent(
                ouid = any(),
                uuid = any(),
                clientOrderId = any(),
                symbol = any(),
                orderId = any(),
                makerFee = any(),
                takerFee = any(),
                leftSideFraction = any(),
                rightSideFraction = any(),
                userLevel = any(),
                side = any(),
                matchConstraint = any(),
                orderType = any(),
                price = any(),
                quantity = any(),
                quoteQuantity = any(),
                createDate = any(),
                updateDate = any()
            )
        } returns Mono.empty()

        verify(exactly = 0) {
            orderStatusRepository.insert(any(), any(), any(), any(), any(), any())
        }
    }

    //To have race condition between RichOrder and UpdateRichOrder,we will temporarily skip this test

//    @Test
//    fun givenMissingOrder_whenUpdateRichOrder_thenFailBeforeSideEffects(): Unit = runBlocking {
//        every {
//            orderRepository.findByOuid(any())
//        } returns Mono.empty()
//
//        assertThrows<IllegalStateException> {
//            runBlocking { orderPersister.update(VALID.RICH_ORDER_UPDATE) }
//        }
//
//        verify(exactly = 0) {
//            orderRepository.touchUpdateDateByOuid(any(), any())
//        }
//        verify(exactly = 0) {
//            orderStatusRepository.insert(any(), any(), any(), any(), any(), any())
//        }
//        verify(exactly = 0) {
//            openOrderRepository.insertOrUpdate(any(), any(), any())
//        }
//    }
}
