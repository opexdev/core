package co.nilin.mixchange.accountant.core.spi

import co.nilin.mixchange.accountant.core.model.FinancialAction
import kotlinx.coroutines.flow.Flow

interface FinancialActionLoader {
    suspend fun findLast(uuid: String, ouid: String): FinancialAction?
    suspend fun loadUnprocessed(offset: Long, size: Long): List<FinancialAction>
    suspend fun countUnprocessed(uuid: String, symbol: String, eventType: String): Long
}