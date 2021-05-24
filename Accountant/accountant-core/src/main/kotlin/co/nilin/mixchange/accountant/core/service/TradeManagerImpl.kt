package co.nilin.mixchange.accountant.core.service

import co.nilin.mixchange.accountant.core.api.TradeManager
import co.nilin.mixchange.accountant.core.model.FinancialAction
import co.nilin.mixchange.accountant.core.spi.*
import co.nilin.mixchange.matching.core.eventh.events.TradeEvent
import co.nilin.mixchange.matching.core.model.OrderDirection
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional
import java.lang.RuntimeException
import java.math.BigDecimal
import java.time.LocalDateTime

open class TradeManagerImpl(
    val pairStaticRateLoader: PairStaticRateLoader,
    val financeActionPersister: FinancialActionPersister,
    val financeActionLoader: FinancialActionLoader,
    val orderPersister: OrderPersister,
    val tempEventPersister: TempEventPersister,
    val walletProxy: WalletProxy,
    val platformCoin: String,
    val platformAddress: String
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
        if ( takerOrder == null || makerOrder == null ){
            if ( takerOrder == null ){
                tempEventPersister.saveTempEvent(trade.takerOuid, trade)
            }
            if ( makerOrder == null ){
                tempEventPersister.saveTempEvent(trade.makerOuid, trade)
            }
            return emptyList()
        }
        //check taker uuid
        //
        //check maker uuid
        //
        val leftSidePCRate = pairStaticRateLoader.calculateStaticRate(platformCoin, trade.pair.leftSideName)
        val rightSidePCRate = pairStaticRateLoader.calculateStaticRate(platformCoin, trade.pair.rightSideName)

        val takerMatchedAmount = if (takerOrder.direction == OrderDirection.ASK) {
            trade.matchedQuantity.toBigDecimal().multiply(takerOrder.leftSideFraction.toBigDecimal())
        } else {
            trade.matchedQuantity.toBigDecimal().multiply(takerOrder.leftSideFraction.toBigDecimal())
                .multiply(trade.makerPrice.toBigDecimal()).multiply(takerOrder.rightSideFraction.toBigDecimal())
        }

        val takerPCFeeCoefficient: Double = if (takerOrder.direction == OrderDirection.ASK) {
            leftSidePCRate ?: 0.0
        } else {
            rightSidePCRate ?: 0.0
        }

        val makerMatchedAmount = if (makerOrder.direction == OrderDirection.ASK) {
            trade.matchedQuantity.toBigDecimal().multiply(makerOrder.leftSideFraction.toBigDecimal())
        } else {
            trade.matchedQuantity.toBigDecimal().multiply(makerOrder.leftSideFraction.toBigDecimal())
                .multiply(trade.takerPrice.toBigDecimal()).multiply(makerOrder.rightSideFraction.toBigDecimal())
        }

        val makerPCFeeCoefficient: Double = if (makerOrder.direction == OrderDirection.ASK) {
            leftSidePCRate ?: 0.0
        } else {
            rightSidePCRate ?: 0.0
        }
        log.info("trade event configs loaded ")
        //lookup for taker parent fa
        val takerParentFinancialAction = financeActionLoader.findLast(trade.takerUuid, trade.takerOuid)
        log.info("trade event takerParentFinancialAction {} ", takerParentFinancialAction)
        //lookup for maker parent fa
        val makerParentFinancialAction = financeActionLoader.findLast(trade.makerUuid, trade.makerOuid)
        log.info("trade event makerParentFinancialAction {} ", makerParentFinancialAction)


        //calculate maker fee
        val makerFee = makerOrder.makerFee
        val makerTotalFeeWithPlatformCoin = takerMatchedAmount
            .multiply(makerFee.toBigDecimal())
            .multiply(makerPCFeeCoefficient.toBigDecimal())
        //check if maker uuid can pay the fee with platform coin
        //create fa for transfer taker uuid symbol exchange wallet to maker symbol main wallet
        /*
        amount for sell (ask): match_quantity (if not pay by platform coin then - maker fee)
        amount for buy (bid): match_quantity * maker price (if not pay by platform coin then - maker fee)
         */
        val takerTransferAction = FinancialAction(
            takerParentFinancialAction,
            TradeEvent::class.simpleName!!,
            trade.takerOuid,
            if (takerOrder.direction == OrderDirection.ASK) {
                trade.pair.leftSideName
            } else {
                trade.pair.rightSideName
            },
            takerMatchedAmount,
            trade.takerUuid,
            "exchange",
            trade.makerUuid,
            "main",
            LocalDateTime.now()
        )
        log.info("trade event takerTransferAction {}", takerTransferAction)
        financialActions.add(takerTransferAction)
        if (makerTotalFeeWithPlatformCoin > BigDecimal.ZERO &&
            walletProxy.canFulfil(platformCoin, "main", trade.makerUuid, makerTotalFeeWithPlatformCoin)) {
            val makerFeeAction = FinancialAction(
                makerParentFinancialAction,
                TradeEvent::class.simpleName!!,
                trade.makerOuid,
                platformCoin,
                makerTotalFeeWithPlatformCoin,
                trade.makerUuid,
                "main",
                platformAddress,
                "exchange",
                LocalDateTime.now()
            )
            log.info("trade event makerFeeAction {}", makerFeeAction)
            financialActions.add(makerFeeAction)
        } else {
            val makerFeeAction = FinancialAction(
                makerParentFinancialAction,
                TradeEvent::class.simpleName!!,
                trade.makerOuid,
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
            log.info("trade event makerFeeAction {}", makerFeeAction)
            financialActions.add(makerFeeAction)
        }
        //update taker order status
        takerOrder.remainedTransferAmount -= takerMatchedAmount
        if (takerOrder.filledQuantity == takerOrder.quantity) {
            takerOrder.status = 1
        }
        orderPersister.save(takerOrder)
        log.info("taker order saved {}", takerOrder)

        //calculate taker fee
        val takerFee = takerOrder.takerFee
        val takerTotalFeeWithPlatformCoin = takerMatchedAmount
            .multiply(takerFee.toBigDecimal())
            .multiply(takerPCFeeCoefficient.toBigDecimal())

        //create fa for transfer makeruuid symbol exchange wallet to taker symbol main wallet
        /*
        amount for sell (ask): match_quantity (if not pay by platform coin then - taker fee)
        amount for buy (bid): match_quantity * maker price (if not pay by platform coin then - taker fee)
         */
        val makerTransferAction = FinancialAction(
            makerParentFinancialAction,
            TradeEvent::class.simpleName!!,
            trade.takerOuid,
            if (makerOrder.direction == OrderDirection.ASK) {
                trade.pair.leftSideName
            } else {
                trade.pair.rightSideName
            },
            makerMatchedAmount,
            trade.makerUuid,
            "exchange",
            trade.takerUuid,
            "main",
            LocalDateTime.now()
        )
        log.info("trade event makerTransferAction {}", makerTransferAction)
        financialActions.add(makerTransferAction)
        //check if taker uuid can pay the fee with platform coin
        if (takerTotalFeeWithPlatformCoin > BigDecimal.ZERO &&
            walletProxy.canFulfil(platformCoin, "main", trade.takerUuid, takerTotalFeeWithPlatformCoin)) {
            val takerFeeAction = FinancialAction(
                takerParentFinancialAction,
                TradeEvent::class.simpleName!!,
                trade.takerOuid,
                if (makerOrder.direction == OrderDirection.ASK) {
                    trade.pair.leftSideName
                } else {
                    trade.pair.rightSideName
                },
                takerTotalFeeWithPlatformCoin,
                trade.takerUuid,
                "main",
                platformAddress,
                "",
                LocalDateTime.now()
            )
            log.info("trade event takerFeeAction {}", takerFeeAction)
            financialActions.add(takerFeeAction)

        } else {
            val takerFeeAction = FinancialAction(
                takerParentFinancialAction,
                TradeEvent::class.simpleName!!,
                trade.takerOuid,
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
            log.info("trade event takerFeeAction {}", takerFeeAction)

            financialActions.add(takerFeeAction)
        }
        //update maker order status
        makerOrder.remainedTransferAmount -= makerMatchedAmount
        if (makerOrder.filledQuantity == makerOrder.quantity) {
            makerOrder.status = 1
        }
        orderPersister.save(makerOrder)
        log.info("maker order saved {}", makerOrder)
        return financeActionPersister.persist(financialActions)
    }
}