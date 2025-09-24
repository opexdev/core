package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.api.FeeCalculator
import co.nilin.opex.accountant.core.api.TradeManager
import co.nilin.opex.accountant.core.inout.OrderStatus
import co.nilin.opex.accountant.core.inout.RichOrderUpdate
import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.accountant.core.model.*
import co.nilin.opex.accountant.core.spi.*
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset

open class TradeManagerImpl(
    private val financeActionPersister: FinancialActionPersister,
    private val financeActionLoader: FinancialActionLoader,
    private val orderPersister: OrderPersister,
    private val tempEventPersister: TempEventPersister,
    private val richTradePublisher: RichTradePublisher,
    private val richOrderPublisher: RichOrderPublisher,
    private val feeCalculator: FeeCalculator,
    private val financialActionPublisher: FinancialActionPublisher,
    private val currencyRatePersister: CurrencyRatePersister,
    private val userVolumePersister: UserVolumePersister,
    private val tradeVolumeCalculationCurrency: String,
    private val zoneOffsetString: String,
) : TradeManager {

    private val logger = LoggerFactory.getLogger(TradeManagerImpl::class.java)

    @Transactional
    override suspend fun handleTrade(trade: TradeEvent): List<FinancialAction> {
//        logger.info("Trade event started ${trade.tradeId}")
        val financialActions = mutableListOf<FinancialAction>()
        //taker order by ouid
        val takerOrder = orderPersister.load(trade.takerOuid)
        //maker order by ouid
        val makerOrder = orderPersister.load(trade.makerOuid)
        if (takerOrder == null || makerOrder == null) {
            if (takerOrder == null) {
                tempEventPersister.saveTempEvent(trade.takerOuid, trade)
            }
            if (makerOrder == null) {
                tempEventPersister.saveTempEvent(trade.makerOuid, trade)
            }
            return emptyList()
        }

        val takerMatchedAmount = if (takerOrder.isAsk()) {
            trade.matchedQuantity.toBigDecimal().multiply(takerOrder.leftSideFraction)
        } else {
            trade.matchedQuantity.toBigDecimal().multiply(takerOrder.leftSideFraction)
                .multiply(trade.makerPrice.toBigDecimal()).multiply(takerOrder.rightSideFraction)
        }

        val makerMatchedAmount = if (makerOrder.isAsk()) {
            trade.matchedQuantity.toBigDecimal().multiply(makerOrder.leftSideFraction)
        } else {
            trade.matchedQuantity.toBigDecimal().multiply(makerOrder.leftSideFraction)
                .multiply(trade.makerPrice.toBigDecimal()).multiply(makerOrder.rightSideFraction)
        }
        logger.info("Trade event configs loaded")

        //lookup for taker parent fa
        val takerParentFinancialAction = financeActionLoader.findLast(trade.takerUuid, trade.takerOuid)
        logger.info("Trade event takerParentFinancialAction ${takerParentFinancialAction?.uuid}")
        //lookup for maker parent fa
        val makerParentFinancialAction = financeActionLoader.findLast(trade.makerUuid, trade.makerOuid)
        logger.info("Trade event makerParentFinancialAction ${makerParentFinancialAction?.uuid}")

        //create fa for transfer taker uuid symbol exchange wallet to maker symbol main wallet
        /*
        amount for sell (ask): match_quantity (if not pay by platform coin then - maker fee)
        amount for buy (bid): match_quantity * maker price (if not pay by platform coin then - maker fee)
         */
        val takerTransferAction = FinancialAction(
            takerParentFinancialAction,
            TradeEvent::class.simpleName!!,
            trade.takerOuid,
            if (takerOrder.isAsk()) trade.pair.leftSideName else trade.pair.rightSideName,
            takerMatchedAmount,
            trade.takerUuid,
            WalletType.EXCHANGE,
            trade.makerUuid,
            WalletType.MAIN,
            LocalDateTime.now(),
            FinancialActionCategory.TRADE
        )
        logger.info("Trade event takerTransferAction ${takerTransferAction.uuid}")
        financialActions.add(takerTransferAction)

        //update taker order status
        takerOrder.remainedTransferAmount -= takerMatchedAmount
        takerOrder.filledQuantity = takerOrder.filledQuantity + trade.matchedQuantity
        takerOrder.filledOrigQuantity = BigDecimal(takerOrder.filledQuantity).multiply(takerOrder.leftSideFraction)

        if (takerOrder.filledQuantity == takerOrder.quantity) {
            takerOrder.status = OrderStatus.FILLED.code
            if (takerOrder.remainedTransferAmount > BigDecimal.ZERO) {
                financialActions.add(createFinalizeOrderFinancialAction(takerParentFinancialAction, takerOrder, trade))
                takerOrder.remainedTransferAmount = BigDecimal.ZERO
            }
        }
        orderPersister.save(takerOrder)
        logger.info("Taker order saved: uuid=${takerOrder.uuid}")
        publishTakerRichOrderUpdate(takerOrder, trade)

        //create fa for transfer makerUuid symbol exchange wallet to taker symbol main wallet
        /*
        amount for sell (ask): match_quantity (if not pay by platform coin then - taker fee)
        amount for buy (bid): match_quantity * maker price (if not pay by platform coin then - taker fee)
         */
        val makerTransferAction = FinancialAction(
            makerParentFinancialAction,
            TradeEvent::class.simpleName!!,
            trade.makerOuid,
            if (makerOrder.isAsk()) trade.pair.leftSideName else trade.pair.rightSideName,
            makerMatchedAmount,
            trade.makerUuid,
            WalletType.EXCHANGE,
            trade.takerUuid,
            WalletType.MAIN,
            LocalDateTime.now(),
            FinancialActionCategory.TRADE
        )
        logger.info("Trade event makerTransferAction ${makerTransferAction.uuid}")
        financialActions.add(makerTransferAction)

        //update maker order status
        makerOrder.remainedTransferAmount -= makerMatchedAmount
        makerOrder.filledQuantity = makerOrder.filledQuantity + trade.matchedQuantity
        makerOrder.filledOrigQuantity = BigDecimal(makerOrder.filledQuantity).multiply(makerOrder.leftSideFraction)
        if (makerOrder.filledQuantity == makerOrder.quantity) {
            makerOrder.status = OrderStatus.FILLED.code
            if (makerOrder.remainedTransferAmount > BigDecimal.ZERO) {
                financialActions.add(createFinalizeOrderFinancialAction(makerParentFinancialAction, makerOrder, trade))
                makerOrder.remainedTransferAmount = BigDecimal.ZERO
            }
        }
        orderPersister.save(makerOrder)
        logger.info("Maker order saved: uuid=${makerOrder.uuid}")
        publishMakerRichOrderUpdate(makerOrder, trade)

        val feeActions = feeCalculator.createFeeActions(
            trade,
            makerOrder,
            takerOrder,
            makerParentFinancialAction,
            takerParentFinancialAction
        ).apply {
            financialActions.add(makerFeeAction)
            financialActions.add(takerFeeAction)
        }

        val takerPrice = trade.takerPrice.toBigDecimal().multiply(takerOrder.rightSideFraction)
        val makerPrice = trade.makerPrice.toBigDecimal().multiply(makerOrder.rightSideFraction)

        val richTrade = RichTrade(
            trade.tradeId,
            trade.pair.toString(),
            trade.takerOuid,
            trade.takerUuid,
            trade.takerOrderId,
            trade.takerDirection,
            takerPrice,
            takerOrder.origQuantity,
            takerOrder.origPrice.multiply(takerOrder.origQuantity),
            trade.takerRemainedQuantity.toBigDecimal().multiply(takerOrder.leftSideFraction),
            feeActions.takerFeeAction.amount,
            feeActions.takerFeeAction.symbol,
            trade.makerOuid,
            trade.makerUuid,
            trade.makerOrderId,
            trade.makerDirection,
            makerPrice,
            makerOrder.origQuantity,
            makerOrder.origPrice.multiply(makerOrder.origQuantity),
            trade.makerRemainedQuantity.toBigDecimal().multiply(makerOrder.leftSideFraction),
            feeActions.makerFeeAction.amount,
            feeActions.makerFeeAction.symbol,
            makerPrice,
            trade.matchedQuantity.toBigDecimal().multiply(makerOrder.leftSideFraction),
            trade.eventDate
        )
        richTradePublisher.publish(richTrade)

        currencyRatePersister.updateRate(trade.pair.leftSideName, trade.pair.rightSideName, makerPrice)
        calculateTradeVolume(richTrade, trade.pair.leftSideName, trade.pair.rightSideName)

        return financeActionPersister.persist(financialActions)
        //return financeActionPersister.persist(financialActions).also { publishFinancialActions(it) }
    }

    private suspend fun calculateTradeVolume(trade: RichTrade, base: String, quote: String) {
        val today = LocalDateTime.now().atOffset(ZoneOffset.of(zoneOffsetString)).toLocalDate()

        val rate = if (quote == tradeVolumeCalculationCurrency) {
            currencyRatePersister.getRate(base, quote)
        } else {
            currencyRatePersister.getRate(quote, tradeVolumeCalculationCurrency)
        }
        val totalAmount = trade.matchedQuantity.multiply(rate)

        userVolumePersister.update(
            trade.makerUuid,
            base,
            today,
            trade.matchedQuantity,
            totalAmount,
            tradeVolumeCalculationCurrency
        )
        userVolumePersister.update(
            trade.takerUuid,
            base,
            today,
            trade.matchedQuantity,
            totalAmount,
            tradeVolumeCalculationCurrency
        )
        logger.info("Trade volume updated")
    }

    private fun createFinalizeOrderFinancialAction(
        parentTransferAction: FinancialAction?,
        order: Order,
        trade: TradeEvent
    ) = FinancialAction(
        parentTransferAction,
        TradeEvent::class.simpleName!!,
        order.ouid,
        if (order.isAsk()) trade.pair.leftSideName else trade.pair.rightSideName,
        order.remainedTransferAmount,
        order.uuid,
        WalletType.EXCHANGE,
        order.uuid,
        WalletType.MAIN,
        LocalDateTime.now(),
        FinancialActionCategory.ORDER_FINALIZED
    )

    private suspend fun publishTakerRichOrderUpdate(takerOrder: Order, trade: TradeEvent) {
        val price = trade.takerPrice.toBigDecimal().multiply(takerOrder.rightSideFraction)
        val remained = trade.takerRemainedQuantity.toBigDecimal().multiply(takerOrder.leftSideFraction)
        publishRichOrderUpdate(takerOrder, price, remained)
    }

    private suspend fun publishMakerRichOrderUpdate(makerOrder: Order, trade: TradeEvent) {
        val price = trade.makerPrice.toBigDecimal().multiply(makerOrder.rightSideFraction)
        val remained = trade.makerRemainedQuantity.toBigDecimal().multiply(makerOrder.leftSideFraction)
        publishRichOrderUpdate(makerOrder, price, remained)
    }

    private suspend fun publishRichOrderUpdate(order: Order, price: BigDecimal, remainedQty: BigDecimal) {
        val status = if (remainedQty.compareTo(BigDecimal.ZERO) == 0)
            OrderStatus.FILLED
        else
            OrderStatus.PARTIALLY_FILLED

        richOrderPublisher.publish(RichOrderUpdate(order.ouid, price, order.origQuantity, remainedQty, status))
    }

    private suspend fun publishFinancialActions(financialActions: List<FinancialAction>) {
        val list = arrayListOf<FinancialAction>()
        financialActions.forEach { extractFAParents(it, list) }
        for (fa in list) {
            if (fa.status == FinancialActionStatus.CREATED) {
                try {
                    financialActionPublisher.publish(fa)
                } catch (e: Exception) {
                    logger.error("Cannot publish fa ${fa.uuid}", e)
                    break
                }
                financeActionPersister.updateStatus(fa.uuid, FinancialActionStatus.PROCESSED)
            }
        }
    }

    private fun extractFAParents(financialAction: FinancialAction, list: ArrayList<FinancialAction>) {
        if (financialAction.parent != null) {
            extractFAParents(financialAction.parent, list)
        }

        if (!list.contains(financialAction))
            list.add(financialAction)
    }
}