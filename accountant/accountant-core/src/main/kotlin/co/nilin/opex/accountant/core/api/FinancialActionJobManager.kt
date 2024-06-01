package co.nilin.opex.accountant.core.api

interface FinancialActionJobManager {

    suspend fun processFinancialActions(offset: Long, size: Long)

    suspend fun retryFinancialActions(limit: Int)
}