package co.nilin.opex.accountant.core.spi

import co.nilin.opex.accountant.core.model.FinancialAction

interface FinancialActionPublisher {
    suspend fun publish(fa: FinancialAction)
}