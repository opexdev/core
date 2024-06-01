package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.FinancialActionResponseEvent

interface FiActionResponseEventSubmitter {

    suspend fun submit(event: FinancialActionResponseEvent)
}