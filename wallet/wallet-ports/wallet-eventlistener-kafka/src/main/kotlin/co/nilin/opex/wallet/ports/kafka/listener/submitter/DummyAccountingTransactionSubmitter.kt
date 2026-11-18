package co.nilin.opex.wallet.ports.kafka.listener.submitter

import co.nilin.opex.wallet.core.model.AccountingTransactionEvent
import co.nilin.opex.wallet.core.spi.AccountingTransactionEventSubmitter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["app.accounting.event.enabled"], havingValue = "false")
class DummyAccountingTransactionSubmitter : AccountingTransactionEventSubmitter {
    override suspend fun send(event: AccountingTransactionEvent) {
    }
}
