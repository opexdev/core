package co.nilin.opex.accountant.core.api

import co.nilin.opex.accountant.core.model.FinancialAction

interface FinancialActionProcessor {

    suspend fun process(financialAction: FinancialAction)

    suspend fun process(financialActions: List<FinancialAction>)

    suspend fun batchProcess(offset: Long, size: Long)
}
