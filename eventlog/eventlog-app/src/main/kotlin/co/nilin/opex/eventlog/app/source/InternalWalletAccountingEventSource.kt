package co.nilin.opex.eventlog.app.source

import co.nilin.opex.eventlog.core.accounting.AccountingEventSource
import co.nilin.opex.eventlog.core.accounting.AccountingTransactionEvent
import co.nilin.opex.eventlog.core.accounting.AccountingTransactionType
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@Component
@ConditionalOnProperty(value = ["app.accounting.mock.enabled"], havingValue = "true")
class InternalWalletAccountingEventSource : AccountingEventSource {

    override suspend fun collectEvents(
        from: LocalDateTime,
        to: LocalDateTime,
        limit: Int
    ): List<AccountingTransactionEvent> {
        val totalMinutes = kotlin.math.max(1L, Duration.between(from, to).toMinutes())
        val count = limit.coerceIn(1, 500)
        return (0 until count).map { index ->
            val minuteOffset = (totalMinutes * (index + 1) / (count + 1)).coerceAtLeast(1)
            val occurredAt = from.plusMinutes(minuteOffset)
            val isDeposit = index % 2 == 0
            AccountingTransactionEvent(
                txId = UUID.nameUUIDFromBytes("wallet-$index-$occurredAt".toByteArray()).toString(),
                userId = UUID.nameUUIDFromBytes("wallet-user-${index % 23}".toByteArray()).toString(),
                type = if (isDeposit) AccountingTransactionType.DEPOSIT else AccountingTransactionType.WITHDRAW,
                symbol = null,
                asset = if (index % 3 == 0) "IRT" else "USDT",
                amount = BigDecimal.valueOf(1_000_000L + (index * 75_000L)),
                fee = if (isDeposit) BigDecimal.ZERO else BigDecimal.valueOf(15_000L + (index * 80L)),
                source = "CORE_WALLET",
                referenceId = "WL-${occurredAt.toLocalDate()}-$index",
                side = null,
                price = null,
                quantity = null,
                occurredAt = occurredAt
            )
        }
    }
}
