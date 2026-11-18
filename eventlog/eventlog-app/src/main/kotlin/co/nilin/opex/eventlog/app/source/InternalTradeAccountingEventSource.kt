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
class InternalTradeAccountingEventSource : AccountingEventSource {

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
            val isSwap = index % 4 == 0
            val symbol = if (index % 2 == 0) "BTCIRT" else "ETHIRT"
            val quantity = BigDecimal.valueOf(1_000L + (index * 25L))
            val price = BigDecimal.valueOf(1_500_000_000L + (index * 2_500_000L))
            AccountingTransactionEvent(
                txId = UUID.nameUUIDFromBytes("trade-$index-$occurredAt".toByteArray()).toString(),
                userId = UUID.nameUUIDFromBytes("trade-user-${index % 17}".toByteArray()).toString(),
                type = if (isSwap) AccountingTransactionType.SWAP else AccountingTransactionType.TRADE,
                symbol = symbol,
                asset = if (symbol.startsWith("BTC")) "BTC" else "ETH",
                amount = quantity.multiply(price),
                fee = BigDecimal.valueOf(25_000L + (index * 125L)),
                source = "CORE_MATCHING_ENGINE",
                referenceId = "ME-${occurredAt.toLocalDate()}-$index",
                side = if (index % 2 == 0) "BUY" else "SELL",
                price = price,
                quantity = quantity,
                occurredAt = occurredAt
            )
        }
    }
}
