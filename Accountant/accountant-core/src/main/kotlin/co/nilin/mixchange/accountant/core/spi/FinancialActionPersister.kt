package co.nilin.mixchange.accountant.core.spi

import co.nilin.mixchange.accountant.core.model.FinancialAction
import co.nilin.mixchange.accountant.core.model.FinancialActionStatus

interface FinancialActionPersister {
    suspend fun persist(financialActions: List<FinancialAction>): List<FinancialAction>
    suspend fun updateStatus(financialAction: FinancialAction, status: FinancialActionStatus )
}