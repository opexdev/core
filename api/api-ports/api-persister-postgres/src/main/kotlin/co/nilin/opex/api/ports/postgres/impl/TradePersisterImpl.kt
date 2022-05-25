package co.nilin.opex.api.ports.postgres.impl

import co.nilin.opex.api.core.event.RichTrade
import co.nilin.opex.api.core.spi.TradePersister
import co.nilin.opex.api.ports.postgres.dao.TradeRepository
import co.nilin.opex.api.ports.postgres.model.TradeModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class TradePersisterImpl(private val tradeRepository: TradeRepository) : TradePersister {

    private val logger = LoggerFactory.getLogger(TradePersisterImpl::class.java)

    @Transactional
    override suspend fun save(trade: RichTrade) {
        tradeRepository.save(
            TradeModel(
                null,
                trade.id,
                trade.pair,
                trade.matchedQuantity,
                trade.takerPrice,
                trade.makerPrice,
                trade.takerCommision,
                trade.makerCommision,
                trade.takerCommisionAsset,
                trade.makerCommisionAsset,
                trade.tradeDateTime,
                trade.makerOuid,
                trade.takerOuid,
                trade.makerUuid,
                trade.takerUuid,
                LocalDateTime.now()
            )
        ).awaitFirstOrNull()
        logger.info("RichTrade ${trade.id} saved")
    }
}