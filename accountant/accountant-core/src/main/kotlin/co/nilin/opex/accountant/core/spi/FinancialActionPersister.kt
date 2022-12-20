package co.nilin.opex.accountant.core.spi

import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.FinancialActionStatus

interface FinancialActionPersister {

    suspend fun persist(financialActions: List<FinancialAction>): List<FinancialAction>

    suspend fun updateStatus(financialAction: FinancialAction, status: FinancialActionStatus)

    suspend fun updateBatchStatus(financialAction: List<FinancialAction>, status: FinancialActionStatus)
}