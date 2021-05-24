package co.nilin.mixchange.wallet.core.spi

import co.nilin.mixchange.wallet.core.model.Transaction

interface TransactionManager {
    suspend fun save(transaction: Transaction): String
}