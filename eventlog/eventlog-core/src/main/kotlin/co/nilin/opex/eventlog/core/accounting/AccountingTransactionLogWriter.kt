package co.nilin.opex.eventlog.core.accounting

interface AccountingTransactionLogWriter {
    suspend fun save(transaction: AccountingTransactionEvent): AccountingTransactionEvent
    suspend fun saveAll(transactions: List<AccountingTransactionEvent>): List<AccountingTransactionEvent>
}
