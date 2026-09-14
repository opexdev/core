package co.nilin.opex.price.core.spi

import co.nilin.opex.price.core.dto.RateHistory

interface RateHistoryPersister {
    suspend fun saveRateHistory(rateHistory: RateHistory)
}
