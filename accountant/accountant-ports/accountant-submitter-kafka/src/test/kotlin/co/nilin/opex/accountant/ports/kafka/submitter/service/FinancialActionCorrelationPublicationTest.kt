package co.nilin.opex.accountant.ports.kafka.submitter.service

import co.nilin.opex.accountant.core.inout.FinancialActionEvent
import co.nilin.opex.accountant.core.model.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.SettableListenableFuture
import java.math.BigDecimal
import java.time.LocalDateTime

class FinancialActionCorrelationPublicationTest {
    @Test
    fun `publishes execution reference with the individual action reference and category`() = runBlocking<Unit> {
        val kafka = mockk<KafkaTemplate<String, FinancialActionEvent>>()
        val events = mutableListOf<FinancialActionEvent>()
        every { kafka.send("fiAction", capture(events)) } answers {
            SettableListenableFuture<SendResult<String, FinancialActionEvent>>().apply { set(mockk()) }
        }
        val submitter = FinancialActionSubmitter(kafka)
        listOf(FinancialActionCategory.TRADE, FinancialActionCategory.FEE).forEach { category ->
            val action = FinancialAction(null, "trade", "order-1", "ETH", BigDecimal.ONE,
                "payer", WalletType.EXCHANGE, "recipient", WalletType.MAIN, LocalDateTime.now(),
                category, operationId = "accountant-trade:execution-1")
            submitter.publish(action)
            assertEquals(action.operationId, events.last().operationId)
            assertEquals("accountant:fiActions:${action.uuid}", events.last().transferRef)
            assertEquals(category.name, events.last().transferCategory)
        }
    }
}
