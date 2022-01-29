package co.nilin.opex.eventlog.ports.postgres.impl

import co.nilin.opex.eventlog.core.spi.Trade
import co.nilin.opex.eventlog.core.spi.TradePersister
import co.nilin.opex.eventlog.ports.postgres.dao.TradeRepository
import co.nilin.opex.eventlog.ports.postgres.model.TradeModel
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TradePersisterImpl(val tradeRepository: TradeRepository) : TradePersister {
    override suspend fun saveTrade(tradeEvent: TradeEvent): Trade {
        return tradeRepository.save(
            TradeModel(
                null,
                tradeEvent.pair.toString(),
                tradeEvent.takerOuid,
                tradeEvent.takerUuid,
                tradeEvent.takerOrderId,
                tradeEvent.takerDirection.toString(),
                tradeEvent.takerPrice,
                tradeEvent.takerRemainedQuantity,
                tradeEvent.makerOuid,
                tradeEvent.makerUuid,
                tradeEvent.makerOrderId,
                tradeEvent.makerDirection.toString(),
                tradeEvent.makerPrice,
                tradeEvent.makerRemainedQuantity,
                tradeEvent.matchedQuantity,
                tradeEvent.eventDate,
                LocalDateTime.now()
            )
        ).awaitFirst()
    }
}