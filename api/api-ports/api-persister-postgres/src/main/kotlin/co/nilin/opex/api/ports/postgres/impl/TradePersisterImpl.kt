package co.nilin.opex.api.ports.postgres.impl

import co.nilin.opex.accountant.core.inout.OrderStatus
import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.accountant.core.inout.comesBefore
import co.nilin.opex.api.core.spi.TradePersister
import co.nilin.opex.api.ports.postgres.dao.OrderRepository
import co.nilin.opex.api.ports.postgres.dao.OrderStatusRepository
import co.nilin.opex.api.ports.postgres.dao.TradeRepository
import co.nilin.opex.api.ports.postgres.model.OrderModel
import co.nilin.opex.api.ports.postgres.model.OrderStatusModel
import co.nilin.opex.api.ports.postgres.model.TradeModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Component
class TradePersisterImpl(
    private val tradeRepository: TradeRepository,
    private val orderStatusRepository: OrderStatusRepository
) : TradePersister {

    private val logger = LoggerFactory.getLogger(TradePersisterImpl::class.java)

    @Transactional
    override suspend fun save(trade: RichTrade) {
        logger.info("RichTrade save")
        tradeRepository.save(
            TradeModel(
                null,
                trade.id,
                trade.pair,
                trade.matchedQuantity.toDouble(),
                trade.takerPrice.toDouble(),
                trade.makerPrice.toDouble(),
                trade.takerCommision.toDouble(),
                trade.makerCommision.toDouble(),
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
        println("RichTrade save/update maker order")
        saveMakerOrder(trade)
        println("RichTrade save/update taker order")
        saveTakerOrder(trade)

    }

    private suspend fun saveTakerOrder(trade: RichTrade) {
        val executedQuantity = (trade.takerQuantity.minus(trade.takerRemainedQuantity)).toDouble()
        val acc = trade.takerPrice.multiply((trade.takerQuantity.minus(trade.takerRemainedQuantity))).toDouble()
        val status = if (trade.takerRemainedQuantity.compareTo(BigDecimal.ZERO) == 0)
            OrderStatus.FILLED
        else
            OrderStatus.PARTIALLY_FILLED

        try {
            orderStatusRepository.save(
                OrderStatusModel(
                    trade.takerOuid,
                    executedQuantity,
                    acc,
                    status.code,
                    status.orderOfAppearance
                )
            ).awaitFirstOrNull()
        } catch (e: Exception) {
            logger.error(e.message)
        }
    }

    private suspend fun saveMakerOrder(trade: RichTrade) {
        val executedQuantity = trade.makerQuantity.minus(trade.makerRemainedQuantity).toDouble()
        val acc = trade.makerPrice.multiply((trade.makerQuantity.minus(trade.makerRemainedQuantity))).toDouble()
        val status = if (trade.makerRemainedQuantity.compareTo(BigDecimal.ZERO) == 0)
            OrderStatus.FILLED
        else
            OrderStatus.PARTIALLY_FILLED

        try {
            orderStatusRepository.save(
                OrderStatusModel(
                    trade.makerOuid,
                    executedQuantity,
                    acc,
                    status.code,
                    status.orderOfAppearance
                )
            ).awaitFirstOrNull()
        } catch (e: Exception) {
            logger.error(e.message)
        }
    }
}