package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.api.TradeManager
import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.Order
import co.nilin.opex.accountant.core.spi.*
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import co.nilin.opex.matching.engine.core.model.OrderDirection
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

open class TradeManagerImpl(
    private val pairStaticRateLoader: PairStaticRateLoader,
    private val financeActionPersister: FinancialActionPersister,
    private val financeActionLoader: FinancialActionLoader,
    private val orderPersister: OrderPersister,
    private val tempEventPersister: TempEventPersister,
    private val richTradePublisher: RichTradePublisher,
    private val walletProxy: WalletProxy,
    private val platformCoin: String,
    private val platformAddress: String
) : TradeManager {

    private val log = LoggerFactory.getLogger(TradeManagerImpl::class.java)

    @Transactional
    override suspend fun handleTrade(trade: TradeEvent): List<FinancialAction> {
        log.info("trade event started {}", trade)
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

        //check taker uuid
        //check maker uuid

        val takerMatchedAmount = calculateMatchedAmount(trade, takerOrder)
        val takerPCFeeCoefficient = getPCFeeCoefficient(trade, takerOrder)

        val makerMatchedAmount = calculateMatchedAmount(trade, makerOrder)
        val makerPCFeeCoefficient = getPCFeeCoefficient(trade, makerOrder)

        log.info("trade event configs loaded ")
        //lookup for taker parent fa
        val takerParentFinancialAction = financeActionLoader.findLast(trade.takerUuid, trade.takerOuid)
        log.info("trade event takerParentFinancialAction {} ", takerParentFinancialAction)
        //lookup for maker parent fa
        val makerParentFinancialAction = financeActionLoader.findLast(trade.makerUuid, trade.makerOuid)
        log.info("trade event makerParentFinancialAction {} ", makerParentFinancialAction)


        //calculate maker fee
        val makerFee = makerOrder.makerFee
        val makerTotalFeeWithPlatformCoin = calcFeeWithPlatformCoin(makerMatchedAmount, makerFee, makerPCFeeCoefficient)

        //check if maker uuid can pay the fee with platform coin
        //create fa for transfer taker uuid symbol exchange wallet to maker symbol main wallet
        /*
        * amount for sell (ask): match_quantity (if not pay by platform coin then - maker fee)
        * amount for buy (bid): match_quantity * maker price (if not pay by platform coin then - maker fee)
        */
        val takerTransferAction =
            createTransferAction(trade, takerParentFinancialAction, takerOrder, takerMatchedAmount)
        log.info("trade event takerTransferAction {}", takerTransferAction)
        financialActions.add(takerTransferAction)

        val makerFeeAction = createMakerFeeAction(
            trade,
            makerParentFinancialAction,
            makerTotalFeeWithPlatformCoin,
            makerFee,
            takerOrder,
            takerMatchedAmount
        )
        log.info("trade event makerFeeAction {}", makerFeeAction)
        financialActions.add(makerFeeAction)

        //update taker order status
        takerOrder.remainedTransferAmount -= takerMatchedAmount
        if (takerOrder.filledQuantity == takerOrder.quantity) {
            takerOrder.status = 1
        }
        orderPersister.save(takerOrder)
        log.info("taker order saved {}", takerOrder)

        //calculate taker fee
        val takerFee = takerOrder.takerFee
        val takerTotalFeeWithPlatformCoin = calcFeeWithPlatformCoin(takerMatchedAmount, takerFee, takerPCFeeCoefficient)

        //create fa for transfer makeruuid symbol exchange wallet to taker symbol main wallet
        /*
        amount for sell (ask): match_quantity (if not pay by platform coin then - taker fee)
        amount for buy (bid): match_quantity * maker price (if not pay by platform coin then - taker fee)
         */
        val makerTransferAction =
            createTransferAction(trade, makerParentFinancialAction, makerOrder, makerMatchedAmount)
        log.info("trade event makerTransferAction {}", makerTransferAction)
        financialActions.add(makerTransferAction)

        //check if taker uuid can pay the fee with platform coin
        val takerFeeAction = createTakerFeeAction(
            trade,
            takerParentFinancialAction,
            takerTotalFeeWithPlatformCoin,
            takerFee,
            makerOrder,
            makerMatchedAmount
        )

        log.info("trade event takerFeeAction {}", takerFeeAction)
        financialActions.add(takerFeeAction)
        //update maker order status
        makerOrder.remainedTransferAmount -= makerMatchedAmount
        if (makerOrder.filledQuantity == makerOrder.quantity) {
            makerOrder.status = 1
        }
        orderPersister.save(makerOrder)
        log.info("maker order saved {}", makerOrder)
        richTradePublisher.publish(
            RichTrade(
                trade.tradeId,
                trade.pair.toString(),
                trade.takerOuid,
                trade.takerUuid,
                trade.takerOrderId,
                trade.takerDirection,
                trade.takerPrice.toBigDecimal().multiply(takerOrder.rightSideFraction.toBigDecimal()),
                takerOrder.origQuantity,
                takerOrder.origPrice.multiply(takerOrder.origQuantity),
                trade.takerRemainedQuantity.toBigDecimal().multiply(takerOrder.leftSideFraction.toBigDecimal()),
                takerFeeAction.amount,
                takerFeeAction.symbol,
                trade.makerOuid,
                trade.makerUuid,
                trade.makerOrderId,
                trade.makerDirection,
                trade.makerPrice.toBigDecimal().multiply(makerOrder.rightSideFraction.toBigDecimal()),
                makerOrder.origQuantity,
                makerOrder.origPrice.multiply(makerOrder.origQuantity),
                trade.makerRemainedQuantity.toBigDecimal().multiply(makerOrder.leftSideFraction.toBigDecimal()),
                makerFeeAction.amount,
                makerFeeAction.symbol,
                trade.matchedQuantity.toBigDecimal().multiply(makerOrder.leftSideFraction.toBigDecimal()),
                trade.eventDate
            )

        )
        return financeActionPersister.persist(financialActions)
    }

    private fun calculateMatchedAmount(trade: TradeEvent, order: Order): BigDecimal {
        return if (order.direction == OrderDirection.ASK) {
            trade.matchedQuantity.toBigDecimal().multiply(order.leftSideFraction.toBigDecimal())
        } else {
            trade.matchedQuantity.toBigDecimal().multiply(order.leftSideFraction.toBigDecimal())
                .multiply(trade.makerPrice.toBigDecimal()).multiply(order.rightSideFraction.toBigDecimal())
        }
    }

    private suspend fun getPCFeeCoefficient(trade: TradeEvent, order: Order): Double {
        val leftSidePCRate = pairStaticRateLoader.calculateStaticRate(platformCoin, trade.pair.leftSideName)
        val rightSidePCRate = pairStaticRateLoader.calculateStaticRate(platformCoin, trade.pair.rightSideName)
        return if (order.direction == OrderDirection.ASK) {
            leftSidePCRate ?: 0.0
        } else {
            rightSidePCRate ?: 0.0
        }
    }

    private fun calcFeeWithPlatformCoin(
        matchedAmount: BigDecimal,
        fee: Double,
        coefficient: Double
    ): BigDecimal {
        return matchedAmount.multiply(fee.toBigDecimal()).multiply(coefficient.toBigDecimal())
    }

    private fun createTransferAction(
        trade: TradeEvent,
        parentAction: FinancialAction?,
        order: Order,
        matchedAmount: BigDecimal
    ): FinancialAction {
        return FinancialAction(
            parentAction,
            TradeEvent::class.simpleName!!,
            trade.takerOuid,
            if (order.direction == OrderDirection.ASK) {
                trade.pair.leftSideName
            } else {
                trade.pair.rightSideName
            },
            matchedAmount,
            trade.takerUuid,
            "exchange",
            trade.makerUuid,
            "main",
            LocalDateTime.now()
        )
    }

    private suspend fun createMakerFeeAction(
        trade: TradeEvent,
        parentAction: FinancialAction?,
        totalFee: BigDecimal,
        makerFee: Double,
        takerOrder: Order,
        takerMatchedAmount: BigDecimal,
    ): FinancialAction {
        val canFulfill = walletProxy.canFulfil(platformCoin, "main", trade.makerUuid, totalFee)
        return if (totalFee > BigDecimal.ZERO && canFulfill) {
            FinancialAction(
                parentAction,
                TradeEvent::class.simpleName!!,
                trade.takerOuid,
                platformCoin,
                totalFee,
                trade.makerUuid,
                "main",
                platformAddress,
                "exchange",
                LocalDateTime.now()
            )
        } else {
            FinancialAction(
                parentAction,
                TradeEvent::class.simpleName!!,
                trade.takerOuid,
                if (takerOrder.direction == OrderDirection.ASK) {
                    trade.pair.leftSideName
                } else {
                    trade.pair.rightSideName
                },
                takerMatchedAmount.multiply(makerFee.toBigDecimal()),
                trade.makerUuid,
                "main",
                platformAddress,
                "exchange",
                LocalDateTime.now()
            )
        }
    }

    private suspend fun createTakerFeeAction(
        trade: TradeEvent,
        parentAction: FinancialAction?,
        totalFee: BigDecimal,
        takerFee: Double,
        makerOrder: Order,
        makerMatchedAmount: BigDecimal,
    ): FinancialAction {
        val canFulfill = walletProxy.canFulfil(platformCoin, "main", trade.takerUuid, totalFee)
        return if (totalFee > BigDecimal.ZERO && canFulfill) {
            FinancialAction(
                parentAction,
                TradeEvent::class.simpleName!!,
                trade.makerOuid,
                if (makerOrder.direction == OrderDirection.ASK) {
                    trade.pair.leftSideName
                } else {
                    trade.pair.rightSideName
                },
                totalFee,
                trade.takerUuid,
                "main",
                platformAddress,
                "",
                LocalDateTime.now()
            )
        } else {
            FinancialAction(
                parentAction,
                TradeEvent::class.simpleName!!,
                trade.makerOuid,
                if (makerOrder.direction == OrderDirection.ASK) {
                    trade.pair.leftSideName
                } else {
                    trade.pair.rightSideName
                },
                makerMatchedAmount.multiply(takerFee.toBigDecimal()),
                trade.takerUuid,
                "main",
                platformAddress,
                "exchange",
                LocalDateTime.now()
            )
        }
    }
}