package co.nilin.opex.accountant.ports.kafka.submitter

import co.nilin.opex.accountant.core.inout.RichOrderEvent
import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.accountant.ports.kafka.submitter.service.RichOrderSubmitter
import co.nilin.opex.accountant.ports.kafka.submitter.service.RichTradeSubmitter
import co.nilin.opex.accountant.ports.kafka.submitter.service.TempEventSubmitter
import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.SettableListenableFuture

class EventPublishersTest {

    private val richOrderTemplate = mockk<KafkaTemplate<String, RichOrderEvent>>()
    private val richOrderSubmitter = RichOrderSubmitter(richOrderTemplate)

    private val richTradeTemplate = mockk<KafkaTemplate<String, RichTrade>>()
    private val richTradeSubmitter = RichTradeSubmitter(richTradeTemplate)

    private val tempEventTemplate = mockk<KafkaTemplate<String, CoreEvent>>()
    private val tempEventSubmitter = TempEventSubmitter(tempEventTemplate)

    init {
        every { richOrderTemplate.send(any(), any()) } returns Valid.kafkaSendFuture<RichOrderEvent>()
        every { richTradeTemplate.send(any(), any()) } returns Valid.kafkaSendFuture<RichTrade>()
        every { tempEventTemplate.send(any(), any()) } returns Valid.kafkaSendFuture<CoreEvent>()
    }

    @Test
    fun givenSubmitters_validateTopics(): Unit = runBlocking {
        assertThat(richOrderSubmitter.topic()).isEqualTo("richOrder")
        assertThat(richTradeSubmitter.topic()).isEqualTo("richTrade")
        assertThat(tempEventSubmitter.topic()).isEqualTo("tempevents")
    }

    @Test
    fun givenRichOrderSubmitter_whenKafkaFailsToSend_throwException(): Unit = runBlocking {
        val future = SettableListenableFuture<SendResult<String, RichOrderEvent>>()
        every { richOrderTemplate.send(any(), any()) } returns future

        future.setException(IllegalStateException("mock"))

        Assertions.assertThatThrownBy {
            runBlocking { richOrderSubmitter.publish(Valid.testRichOrder) }
        }.isInstanceOfAny(Throwable::class.java)
    }

    @Test
    fun givenRichTradeSubmitter_whenKafkaFailsToSend_throwException(): Unit = runBlocking {
        val future = SettableListenableFuture<SendResult<String, RichTrade>>()
        every { richTradeTemplate.send(any(), any()) } returns future

        future.setException(IllegalStateException("mock"))

        Assertions.assertThatThrownBy {
            runBlocking { richTradeSubmitter.publish(Valid.richTrade) }
        }.isInstanceOfAny(Throwable::class.java)
    }

    @Test
    fun givenTempEventSubmitter_whenKafkaFailsToSend_throwException(): Unit = runBlocking {
        val future = SettableListenableFuture<SendResult<String, CoreEvent>>()
        every { tempEventTemplate.send(any(), any()) } returns future

        future.setException(IllegalStateException("mock"))

        Assertions.assertThatThrownBy {
            runBlocking { tempEventSubmitter.republish(listOf(Valid.testCoreEvent)) }
        }.isInstanceOfAny(Throwable::class.java)
    }

    @Test
    fun givenRichOrderSubmitter_whenPublish_callSendWithCorrectTopic():Unit = runBlocking {
        richOrderSubmitter.publish(Valid.testRichOrder)
        verify { richOrderTemplate.send(eq(richOrderSubmitter.topic()),any()) }
    }

    @Test
    fun givenTradeOrderSubmitter_whenPublish_callSendWithCorrectTopic():Unit = runBlocking {
        richTradeSubmitter.publish(Valid.richTrade)
        verify { richTradeTemplate.send(eq(richTradeSubmitter.topic()),any()) }
    }

    @Test
    fun givenTempEventSubmitter_whenRepublish_callSendForEachEventWithCorrectTopic(): Unit = runBlocking {
        tempEventSubmitter.republish(listOf(Valid.testCoreEvent,Valid.testCoreEvent))
        verify(exactly = 2) { tempEventTemplate.send(eq(tempEventSubmitter.topic()),any()) }
    }

}