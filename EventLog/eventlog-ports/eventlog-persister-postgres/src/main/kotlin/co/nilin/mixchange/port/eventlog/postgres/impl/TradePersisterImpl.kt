package co.nilin.mixchange.port.eventlog.postgres.impl

import co.nilin.mixchange.eventlog.spi.Trade
import co.nilin.mixchange.eventlog.spi.TradePersister
import co.nilin.mixchange.matching.core.eventh.events.TradeEvent
import co.nilin.mixchange.port.eventlog.postgres.dao.TradeRepository
import co.nilin.mixchange.port.eventlog.postgres.model.TradeModel
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