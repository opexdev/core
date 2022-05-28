package co.nilin.opex.accountant.ports.kafka.listener

import co.nilin.opex.accountant.ports.kafka.listener.consumer.ConsumerObject
import co.nilin.opex.accountant.ports.kafka.listener.consumer.ListenerObject
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ConsumerTest {

    private val consumer = ConsumerObject()
    private val listener = mockk<ListenerObject>()

    init {
        coEvery { listener.onEvent(any(), any(), any(), any()) } returns Unit
    }

    @Test
    fun givenEventConsumer_onMessage_callListener() {
        consumer.addListener(listener)
        consumer.onMessage(ConsumerRecord("topic", 1, 0, null, "value"))

        coVerify(exactly = consumer.countListeners()) { listener.onEvent(eq("value"), eq(1), eq(0), any()) }
    }

    @Test
    fun givenEventConsumer_onMessageWith2Listeners_callListener() {
        consumer.addListener(listener)
        consumer.addListener(listener)
        consumer.onMessage(ConsumerRecord("topic", 1, 0, null, "value"))

        coVerify(exactly = 2) { listener.onEvent(eq("value"), eq(1), eq(0), any()) }
    }

    @Test
    fun givenEventConsumer_whenAdding1Listener_listenerCountIs1() {
        consumer.addListener(listener)
        assertThat(consumer.countListeners()).isEqualTo(1)
    }

    @Test
    fun givenEventConsumer_whenAdding1ListenerAndRemoving1_listenerCountIs0() {
        consumer.addListener(listener)
        coEvery { listener.id() } returns "L1"
        consumer.removeListener(listener)
        assertThat(consumer.countListeners()).isEqualTo(0)
    }

}