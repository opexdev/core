package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.market.core.event.RichTrade
import co.nilin.opex.market.core.inout.MarketTrade
import co.nilin.opex.market.core.inout.OrderDirection
import co.nilin.opex.market.core.spi.TradePersister
import co.nilin.opex.market.ports.postgres.dao.TradeRepository
import co.nilin.opex.market.ports.postgres.model.TradeModel
import co.nilin.opex.market.ports.postgres.util.RedisCacheHelper
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Component
class TradePersisterImpl(
    private val tradeRepository: TradeRepository,
    private val redisCacheHelper: RedisCacheHelper,
) : TradePersister {

    private val logger = LoggerFactory.getLogger(TradePersisterImpl::class.java)

    override suspend fun save(trade: RichTrade) {
        val pair = trade.pair.split("_")
        val tradeModel = TradeModel(
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

        val tradeEntity = try {
            tradeRepository.save(tradeModel).awaitFirstOrNull()
        } catch (e: DuplicateKeyException) {
            ensureNotCollision(tradeModel, trade)
            return
        } catch (e: DataIntegrityViolationException) {
            if (!isDuplicateTradeViolation(e)) {
                throw e
            }
            ensureNotCollision(tradeModel, trade)
            return
        }
        logger.info("RichTrade ${trade.id} saved")
        //calculateTradeVolume(trade, pair[0].uppercase(), pair[1].uppercase()) // Moved to accountant
        updateCache(trade, tradeEntity)
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

    private suspend fun ensureNotCollision(incomingTrade: TradeModel, originalTrade: RichTrade) {
        val existingTrade = tradeRepository.findBySymbolAndTradeId(
            incomingTrade.symbol,
            incomingTrade.tradeId
        ).awaitFirstOrNull() ?: throw IllegalStateException(
            "Duplicate trade conflict detected but existing row not found for symbol=${incomingTrade.symbol}, tradeId=${incomingTrade.tradeId}"
        )

        if (isSameTradePayload(existingTrade, incomingTrade)) {
            logger.info("RichTrade ${incomingTrade.tradeId} for ${incomingTrade.symbol} is duplicate delivery; skipping")
            return
        }

        // Real ID collision (e.g. Redis counter reset): persist under a new synthetic ID and continue
        logger.error(
            "Trade ID collision for symbol=${incomingTrade.symbol}, tradeId=${incomingTrade.tradeId}. " +
                "Saving colliding trade under a new synthetic ID."
        )
        val newId = Math.abs(UUID.randomUUID().mostSignificantBits)
        val reassigned = TradeModel(
            null,
            newId,
            incomingTrade.symbol,
            incomingTrade.baseAsset,
            incomingTrade.quoteAsset,
            incomingTrade.matchedPrice,
            incomingTrade.matchedQuantity,
            incomingTrade.takerPrice,
            incomingTrade.makerPrice,
            incomingTrade.takerCommission,
            incomingTrade.makerCommission,
            incomingTrade.takerCommissionAsset,
            incomingTrade.makerCommissionAsset,
            incomingTrade.tradeDate,
            incomingTrade.makerOuid,
            incomingTrade.takerOuid,
            incomingTrade.makerUuid,
            incomingTrade.takerUuid,
            incomingTrade.createDate
        )
        val saved = tradeRepository.save(reassigned).awaitFirstOrNull()
        updateCache(originalTrade, saved)
    }

    private fun isSameTradePayload(existing: TradeModel, incoming: TradeModel): Boolean {
        return existing.makerOuid == incoming.makerOuid &&
            existing.takerOuid == incoming.takerOuid &&
            existing.matchedPrice.compareTo(incoming.matchedPrice) == 0 &&
            existing.matchedQuantity.compareTo(incoming.matchedQuantity) == 0 &&
            existing.tradeDate == incoming.tradeDate &&
            existing.makerCommission == incoming.makerCommission &&
            existing.takerCommission == incoming.takerCommission &&
            existing.makerCommissionAsset == incoming.makerCommissionAsset &&
            existing.takerCommissionAsset == incoming.takerCommissionAsset
    }

    private fun isDuplicateTradeViolation(exception: Throwable): Boolean {
        val errorText = buildString {
            append(exception.message.orEmpty())
            append(' ')
            append(exception.cause?.message.orEmpty())
        }
        return errorText.contains("uq_trades_symbol_trade_id", ignoreCase = true) ||
            errorText.contains("duplicate key value", ignoreCase = true)
    }
}