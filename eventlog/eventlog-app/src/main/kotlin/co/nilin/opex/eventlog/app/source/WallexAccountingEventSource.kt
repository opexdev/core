package co.nilin.opex.eventlog.app.source

import co.nilin.opex.eventlog.app.mock.MockWallexApiClient
import co.nilin.opex.eventlog.core.accounting.AccountingEventSource
import co.nilin.opex.eventlog.core.accounting.AccountingTransactionEvent
import co.nilin.opex.eventlog.core.accounting.AccountingTransactionType
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@ConditionalOnProperty(value = ["app.accounting.mock.external-enabled"], havingValue = "true")
class WallexAccountingEventSource(
    private val wallexApiClient: MockWallexApiClient
) : AccountingEventSource {

    override suspend fun collectEvents(
        from: LocalDateTime,
        to: LocalDateTime,
        limit: Int
    ): List<AccountingTransactionEvent> {
        return wallexApiClient.fetchWalletMovements(from, to, limit)
            .map { movement ->
                AccountingTransactionEvent(
                    txId = movement.txId,
                    userId = movement.userId,
                    type = if (movement.side == "DEPOSIT") {
                        AccountingTransactionType.DEPOSIT
                    } else {
                        AccountingTransactionType.WITHDRAW
                    },
                    symbol = null,
                    asset = movement.asset,
                    amount = movement.amount,
                    fee = movement.fee,
                    source = "WALLEX",
                    referenceId = movement.referenceId,
                    side = null,
                    price = null,
                    quantity = null,
                    occurredAt = movement.occurredAt
                )
            }
    }
}
