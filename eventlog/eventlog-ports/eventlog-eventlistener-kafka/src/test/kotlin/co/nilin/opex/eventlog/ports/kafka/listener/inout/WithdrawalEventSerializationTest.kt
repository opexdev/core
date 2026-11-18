package co.nilin.opex.eventlog.ports.kafka.listener.inout

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class WithdrawalEventSerializationTest {

    private val mapper = jacksonObjectMapper()

    @Test
    fun `deserializes old withdraw request payload`() {
        val payload = """
            {
              "uuid":"user-1",
              "withdrawId":12,
              "currency":"USDT",
              "amount":100.5,
              "status":"DONE",
              "createDate":"2026-11-16T10:00:00"
            }
        """.trimIndent()

        val event: WithdrawRequestEvent = mapper.readValue(payload)

        assertThat(event.withdrawUuid).isNull()
        assertThat(event.lifecycleEventId).isNull()
        assertThat(event.status).isEqualTo("DONE")
    }

    @Test
    fun `serializes and deserializes extended accounting payload`() {
        val now = LocalDateTime.parse("2026-11-16T11:30:00")
        val event = AccountingTransactionKafkaEvent(
            txId = "event-1",
            eventId = "event-1",
            userId = "user-1",
            type = "WITHDRAW",
            symbol = "USDT",
            asset = "USDT",
            amount = BigDecimal("11.00"),
            fee = BigDecimal("1.00"),
            feeAsset = "USDT",
            principalAmount = BigDecimal("10.00"),
            grossAmount = BigDecimal("11.00"),
            withdrawStatus = "DONE",
            transferCategory = "WITHDRAW_ACCEPT",
            operationId = "withdraw-1",
            businessReference = "wallet:withdraw:withdraw-1:DONE",
            walletTransactionRef = "9001",
            recordRole = "WITHDRAW_LIFECYCLE",
            source = "CORE_WALLET",
            referenceId = "withdraw-1",
            side = "OUT",
            price = null,
            quantity = null,
            operationCreatedAt = now.minusMinutes(2),
            lifecycleTransitionAt = now,
            completedAt = now,
            occurredAt = now
        )

        val encoded = mapper.writeValueAsString(event)
        val decoded: AccountingTransactionKafkaEvent = mapper.readValue(encoded)

        assertThat(decoded.eventId).isEqualTo("event-1")
        assertThat(decoded.operationId).isEqualTo("withdraw-1")
        assertThat(decoded.principalAmount).isEqualTo(BigDecimal("10.00"))
        assertThat(decoded.grossAmount).isEqualTo(BigDecimal("11.00"))
    }
}
