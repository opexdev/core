package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.market.core.event.RichTrade
import co.nilin.opex.market.core.inout.MarketTrade
import co.nilin.opex.market.core.inout.OrderDirection
import co.nilin.opex.market.core.inout.RateSource
import co.nilin.opex.market.core.spi.TradePersister
import co.nilin.opex.market.ports.postgres.dao.CurrencyRateRepository
import co.nilin.opex.market.ports.postgres.dao.TradeRepository
import co.nilin.opex.market.ports.postgres.model.TradeModel
import co.nilin.opex.market.ports.postgres.util.CacheHelper
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList

@Component
class TradePersisterImpl(
    private val tradeRepository: TradeRepository,
    private val currencyRateRepository: CurrencyRateRepository,
    private val cacheHelper: CacheHelper
) : TradePersister {

    private val logger = LoggerFactory.getLogger(TradePersisterImpl::class.java)

    @Transactional
    override suspend fun save(trade: RichTrade) {
        val pair = trade.pair.split("_")

        val tradeEntity = tradeRepository.save(
            TradeModel(
                null,
                trade.id,
                trade.pair,
                pair[0].uppercase(),
                pair[1].uppercase(),
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

        currencyRateRepository.createOrUpdate(
            pair[0].uppercase(),
            pair[1].uppercase(),
            RateSource.MARKET,
            trade.matchedPrice
        ).awaitFirstOrNull()
        logger.info("Rate between ${pair[0]} and ${pair[1]} updated")

        val recentTradesCache = cacheHelper.get("recentTrades") ?: return
        if (tradeEntity == null) return

        try {
            with(ArrayList<MarketTrade>()) {
                val isMakerBuyer = trade.makerDirection == OrderDirection.BID
                add(
                    MarketTrade(
                        tradeEntity.symbol,
                        tradeEntity.baseAsset,
                        tradeEntity.quoteAsset,
                        tradeEntity.tradeId,
                        tradeEntity.matchedPrice,
                        tradeEntity.matchedQuantity,
                        if (isMakerBuyer)
                            trade.makerQuoteQuantity
                        else
                            trade.takerQuoteQuantity,
                        Date.from(tradeEntity.createDate.atZone(ZoneId.systemDefault()).toInstant()),
                        true,
                        isMakerBuyer
                    )
                )
                addAll(recentTradesCache as Collection<MarketTrade>)
                cacheHelper.put("recentTrades",this)
                logger.info("Recent trades cache updated")
            }
        }catch (e:Exception){
            logger.info("Could not update recentTrades cache")
            cacheHelper.evict("recentTrades")
        }

    }
}