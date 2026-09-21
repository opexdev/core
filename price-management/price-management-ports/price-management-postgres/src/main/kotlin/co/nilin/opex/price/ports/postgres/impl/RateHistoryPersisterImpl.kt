package co.nilin.opex.price.ports.postgres.impl

import co.nilin.opex.price.core.dto.RateHistory
import co.nilin.opex.price.core.spi.RateHistoryPersister
import co.nilin.opex.price.ports.postgres.dao.RateHistoryRepository
import co.nilin.opex.price.ports.postgres.utils.asModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

@Component
class RateHistoryPersisterImpl(
    private val rateHistoryRepository: RateHistoryRepository
) : RateHistoryPersister {

    override suspend fun saveRateHistory(rateHistory: RateHistory) {
        rateHistoryRepository.save(rateHistory.asModel()).awaitFirstOrNull()
    }
}
