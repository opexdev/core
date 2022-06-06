package co.nilin.opex.accountant.ports.postgres

import co.nilin.opex.accountant.ports.postgres.dao.OrderRepository
import co.nilin.opex.accountant.ports.postgres.impl.OrderPersisterImpl
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

class OrderPersisterImplTest {

    private val repository = mockk<OrderRepository> {
        every { save(any()) } returns Mono.just(Valid.orderModel)
        every { findByOuid(any()) } returns Mono.just(Valid.orderModel)
    }

    private val persister = OrderPersisterImpl(repository)

    @Test
    fun givenOUID_whenLoading_resultNotNull(): Unit = runBlocking {
        val order = persister.load("")
        assertThat(order).isNotNull
    }

    @Test
    fun givenOUID_whenLoading_resultIsValidOrder(): Unit = runBlocking {
        val order = persister.load("")!!
        coVerify { repository.findByOuid(any()) }
        assertThat(order.status).isEqualTo(Valid.orderModel.status)
        assertThat(order.matchingEngineId).isEqualTo(Valid.orderModel.matchingEngineId)
        assertThat(order.direction).isEqualTo(Valid.orderModel.direction)
        assertThat(order.filledQuantity).isEqualTo(Valid.orderModel.filledQuantity)
        assertThat(order.ouid).isEqualTo(Valid.orderModel.ouid)
    }

    @Test
    fun givenNewOrder_whenSaving_saveAndReturnValidOrder(): Unit = runBlocking {
        val newOrder = persister.save(Valid.order)
        coVerify { repository.save(any()) }
        with(Valid.order){
            assertThat(newOrder).isNotNull
            assertThat(status).isEqualTo(Valid.orderModel.status)
            assertThat(matchingEngineId).isEqualTo(Valid.orderModel.matchingEngineId)
            assertThat(direction).isEqualTo(Valid.orderModel.direction)
            assertThat(filledQuantity).isEqualTo(Valid.orderModel.filledQuantity)
            assertThat(ouid).isEqualTo(Valid.orderModel.ouid)
        }
    }

}