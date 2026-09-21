package co.nilin.opex.price.ports.postgres.impl

import co.nilin.opex.price.core.dto.RateHistory
import co.nilin.opex.price.core.spi.RateHistoryLoader
import co.nilin.opex.price.ports.postgres.dao.RateHistoryRepository
import co.nilin.opex.price.ports.postgres.utils.asCoreModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class RateHistoryLoaderImpl(
    private val rateHistoryRepository: RateHistoryRepository
) : RateHistoryLoader {

    override suspend fun loadRateHistory(symbol: String, limit: Int): List<RateHistory> {
        return rateHistoryRepository.findBySymbolWithLimit(symbol, limit)
            .collectList()
            .awaitFirstOrNull()
            ?.map { it.asCoreModel() }
            ?: emptyList()
    }

    override suspend fun loadLatest(symbol: String): RateHistory? {
        return rateHistoryRepository.findLatestBySymbol(symbol)
            .awaitFirstOrNull()
            ?.asCoreModel()
    }

    override suspend fun loadLatestForAllSymbols(): List<RateHistory> {
        return rateHistoryRepository.findLatestForAllSymbols()
            .collectList()
            .awaitFirstOrNull()
            ?.map { it.asCoreModel() }
            ?: emptyList()
    }

    override suspend fun loadHistory(symbol: String, startTime: LocalDateTime, endTime: LocalDateTime): List<RateHistory> {
        return rateHistoryRepository.findBySymbolAndCreatedDateBetween(symbol, startTime, endTime)
            .collectList()
            .awaitFirstOrNull()
            ?.map { it.asCoreModel() }
            ?: emptyList()
    }

    override suspend fun loadHistoryForAllSymbols(startTime: LocalDateTime, endTime: LocalDateTime): List<RateHistory> {
        return rateHistoryRepository.findAllByCreatedDateBetween(startTime, endTime)
            .collectList()
            .awaitFirstOrNull()
            ?.map { it.asCoreModel() }
            ?: emptyList()
    }
}
