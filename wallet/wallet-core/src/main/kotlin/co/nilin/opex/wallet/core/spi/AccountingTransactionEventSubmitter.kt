package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.model.AccountingTransactionEvent

interface AccountingTransactionEventSubmitter {
    suspend fun send(event: AccountingTransactionEvent)
}
