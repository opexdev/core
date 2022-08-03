package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.market.core.event.RichTrade
import co.nilin.opex.market.core.inout.RateSource
import co.nilin.opex.market.core.spi.TradePersister
import co.nilin.opex.market.ports.postgres.dao.CurrencyRateRepository
import co.nilin.opex.market.ports.postgres.dao.TradeRepository
import co.nilin.opex.market.ports.postgres.model.TradeModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class TradePersisterImpl(
    private val tradeRepository: TradeRepository,
    private val currencyRateRepository: CurrencyRateRepository
) : TradePersister {

    private val logger = LoggerFactory.getLogger(TradePersisterImpl::class.java)

    @Transactional
    override suspend fun save(trade: RichTrade) {
        tradeRepository.save(
            TradeModel(
                null,
                trade.id,
                trade.pair,
                trade.matchedPrice,
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

        val pair = trade.pair.split("_")
        currencyRateRepository.createOrUpdate(
            pair[0].uppercase(),
            pair[1].uppercase(),
            RateSource.MARKET,
            trade.matchedPrice
        ).awaitFirstOrNull()
        logger.info("Rate between ${pair[0]} and ${pair[1]} updated")
    }
}