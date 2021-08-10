package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.model.Transaction

interface TransactionManager {
    suspend fun save(transaction: Transaction): String
}