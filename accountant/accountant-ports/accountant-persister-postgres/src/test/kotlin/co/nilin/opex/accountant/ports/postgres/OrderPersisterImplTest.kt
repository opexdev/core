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
        every { save(any()) } returns Mono.just(DOC.orderModel)
        every { findByOuid(any()) } returns Mono.just(DOC.orderModel)
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
        assertThat(order.status).isEqualTo(DOC.orderModel.status)
        assertThat(order.matchingEngineId).isEqualTo(DOC.orderModel.matchingEngineId)
        assertThat(order.direction).isEqualTo(DOC.orderModel.direction)
        assertThat(order.filledQuantity).isEqualTo(DOC.orderModel.filledQuantity)
        assertThat(order.ouid).isEqualTo(DOC.orderModel.ouid)
    }

    @Test
    fun givenNewOrder_whenSaving_saveAndReturnValidOrder(): Unit = runBlocking {
        val newOrder = persister.save(DOC.order)
        coVerify { repository.save(any()) }
        with(DOC.order){
            assertThat(newOrder).isNotNull
            assertThat(status).isEqualTo(DOC.orderModel.status)
            assertThat(matchingEngineId).isEqualTo(DOC.orderModel.matchingEngineId)
            assertThat(direction).isEqualTo(DOC.orderModel.direction)
            assertThat(filledQuantity).isEqualTo(DOC.orderModel.filledQuantity)
            assertThat(ouid).isEqualTo(DOC.orderModel.ouid)
        }
    }

}