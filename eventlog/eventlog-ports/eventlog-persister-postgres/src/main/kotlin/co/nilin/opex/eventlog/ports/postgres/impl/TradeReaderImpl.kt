package co.nilin.opex.eventlog.ports.postgres.impl

import co.nilin.opex.eventlog.core.accounting.TradeReader
import co.nilin.opex.eventlog.core.accounting.TradeRecord
import co.nilin.opex.eventlog.ports.postgres.dao.TradeRepository
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TradeReaderImpl(
    private val tradeRepository: TradeRepository
) : TradeReader {
    override suspend fun findBetween(
        from: LocalDateTime,
        to: LocalDateTime,
        limit: Int
    ): List<TradeRecord> {
        val page = PageRequest.of(0, limit.coerceIn(1, 10_000))
        return tradeRepository.findByEventDateBetweenOrderByEventDateDesc(from, to, page)
            .map {
                TradeRecord(
                    id = it.id,
                    symbol = it.symbol,
                    takerUuid = it.takerUuid,
                    makerUuid = it.makerUuid,
                    takerPrice = it.takerPrice,
                    matchedQuantity = it.matchedQuantity,
                    tradeDate = it.eventDate
                )
            }
            .collectList()
            .awaitFirst()
    }
}
