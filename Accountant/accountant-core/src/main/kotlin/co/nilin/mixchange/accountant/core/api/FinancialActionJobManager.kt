package co.nilin.mixchange.accountant.core.api

interface FinancialActionJobManager {
    suspend fun processFinancialActions(offset: Long, size: Long)
}
