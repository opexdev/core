package co.nilin.opex.port.eventlog.postgres.impl

import co.nilin.opex.eventlog.spi.Trade
import co.nilin.opex.eventlog.spi.TradePersister
import co.nilin.opex.matching.core.eventh.events.TradeEvent
import co.nilin.opex.port.eventlog.postgres.dao.TradeRepository
import co.nilin.opex.port.eventlog.postgres.model.TradeModel
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