package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.market.core.event.RichTrade
import co.nilin.opex.market.core.inout.MarketTrade
import co.nilin.opex.market.core.inout.OrderDirection
import co.nilin.opex.market.core.inout.RateSource
import co.nilin.opex.market.core.spi.TradePersister
import co.nilin.opex.market.ports.postgres.dao.CurrencyRateRepository
import co.nilin.opex.market.ports.postgres.dao.TradeRepository
import co.nilin.opex.market.ports.postgres.dao.UserTradeVolumeRepository
import co.nilin.opex.market.ports.postgres.model.TradeModel
import co.nilin.opex.market.ports.postgres.util.RedisCacheHelper
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Component
class TradePersisterImpl(
    private val tradeRepository: TradeRepository,
    private val currencyRateRepository: CurrencyRateRepository,
    private val redisCacheHelper: RedisCacheHelper,
    private val tradeVolumeRepository: UserTradeVolumeRepository
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

        //calculateTradeVolume(trade, pair[0].uppercase(), pair[1].uppercase()) // Moved to accountant
        updateCache(trade, tradeEntity)
    }

    private suspend fun calculateTradeVolume(trade: RichTrade, base: String, quote: String) {
        val today = LocalDate.now()

        var valueUSDT = BigDecimal.ZERO
        var valueIRT = BigDecimal.ZERO

        if (quote.equals("IRT", true)) {
            val baseUSDTRate = currencyRateRepository.findByBaseAndQuoteAndSource(base, "USDT", RateSource.MARKET)
                .awaitFirstOrNull()?.rate ?: BigDecimal.ZERO
            valueUSDT = trade.matchedQuantity * baseUSDTRate
            valueIRT = trade.matchedQuantity * trade.matchedPrice
        } else if (quote.equals("USDT", true)) {
            val baseIRTRate = currencyRateRepository.findByBaseAndQuoteAndSource(base, "IRT", RateSource.MARKET)
                .awaitFirstOrNull()?.rate ?: BigDecimal.ZERO
            valueUSDT = trade.matchedQuantity * trade.matchedPrice
            valueIRT = trade.matchedQuantity * baseIRTRate
        }

        tradeVolumeRepository.insertOrUpdate(
            trade.makerUuid,
            base,
            today,
            trade.matchedQuantity,
            valueUSDT,
            valueIRT
        ).awaitSingleOrNull()
        tradeVolumeRepository.insertOrUpdate(
            trade.takerUuid,
            base,
            today,
            trade.matchedQuantity,
            valueUSDT,
            valueIRT
        ).awaitFirstOrNull()
        logger.info("Trade volume updated")
    }

    private fun updateCache(trade: RichTrade, tradeEntity: TradeModel?) {
        try {
            if (tradeEntity == null || !redisCacheHelper.hasKey("recentTrades:${trade.pair.lowercase()}")) return
            val isMakerBuyer = trade.makerDirection == OrderDirection.BID
            redisCacheHelper.putListItem(
                "recentTrades:${trade.pair.lowercase()}",
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
                ),
                false
            )

            logger.info("Recent trades cache updated")
        } catch (e: Exception) {
            logger.info("Could not update recentTrades cache")
        }
    }
}