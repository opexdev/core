package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.api.FeeCalculator
import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.Order
import co.nilin.opex.accountant.core.spi.FinancialActionLoader
import co.nilin.opex.accountant.core.spi.FinancialActionPersister
import co.nilin.opex.accountant.core.spi.PairStaticRateLoader
import co.nilin.opex.accountant.core.spi.WalletProxy
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDateTime

class FeeCalculatorImpl(
    private val financeActionPersister: FinancialActionPersister,
    private val financeActionLoader: FinancialActionLoader,
    private val pairStaticRateLoader: PairStaticRateLoader,
    private val walletProxy: WalletProxy,
    private val platformCoin: String,
    private val platformAddress: String
) : FeeCalculator {

    private val logger = LoggerFactory.getLogger(FeeCalculatorImpl::class.java)

    override suspend fun createMakerFeeAction(trade: TradeEvent, makerOrder: Order, takerOrder: Order) {
        val actions = mutableListOf<FinancialAction>()
        logger.info("Start fee calculation for trade ${trade.takerUuid}")

        // Look up parent financial actions
        val makerParentFinancialAction = financeActionLoader.findLast(trade.makerUuid, trade.makerOuid)
        val takerParentFinancialAction = financeActionLoader.findLast(trade.takerUuid, trade.takerOuid)
        logger.info("Parent financial actions loaded")

        // TODO cache this
        val leftSidePCRate = pairStaticRateLoader.calculateStaticRate(platformCoin, trade.pair.leftSideName) ?: 0.0
        val rightSidePCRate = pairStaticRateLoader.calculateStaticRate(platformCoin, trade.pair.rightSideName) ?: 0.0

        val makerPCFeeCoefficient = if (makerOrder.isAsk()) leftSidePCRate else rightSidePCRate
        val takerPCFeeCoefficient = if (takerOrder.isAsk()) leftSidePCRate else rightSidePCRate

        val makerMatchedAmount = if (makerOrder.isAsk()) {
            trade.matchedQuantity.toBigDecimal().multiply(makerOrder.leftSideFraction.toBigDecimal())
        } else {
            trade.matchedQuantity.toBigDecimal()
                .multiply(makerOrder.leftSideFraction.toBigDecimal())
                .multiply(trade.makerPrice.toBigDecimal())
                .multiply(makerOrder.rightSideFraction.toBigDecimal())
        }

        val takerMatchedAmount = if (takerOrder.isAsk()) {
            trade.matchedQuantity.toBigDecimal().multiply(takerOrder.leftSideFraction.toBigDecimal())
        } else {
            trade.matchedQuantity.toBigDecimal()
                .multiply(takerOrder.leftSideFraction.toBigDecimal())
                .multiply(trade.makerPrice.toBigDecimal())
                .multiply(takerOrder.rightSideFraction.toBigDecimal())
        }

        //calculate maker fee
        val makerFee = makerOrder.makerFee
        val makerTotalFeeWithPlatformCoin = takerMatchedAmount
            .multiply(makerFee.toBigDecimal())
            .multiply(makerPCFeeCoefficient.toBigDecimal())

        //check if maker uuid can pay the fee with platform coin
        val makerFeeAction = if (makerTotalFeeWithPlatformCoin > BigDecimal.ZERO &&
            walletProxy.canFulfil(platformCoin, "main", trade.makerUuid, makerTotalFeeWithPlatformCoin)
        ) {
            FinancialAction(
                makerParentFinancialAction,
                TradeEvent::class.simpleName!!,
                trade.takerOuid,
                platformCoin,
                makerTotalFeeWithPlatformCoin,
                trade.makerUuid,
                "main",
                platformAddress,
                "exchange",
                LocalDateTime.now()
            )
        } else {
            FinancialAction(
                makerParentFinancialAction,
                TradeEvent::class.simpleName!!,
                trade.takerOuid,
                if (takerOrder.isAsk()) trade.pair.leftSideName else trade.pair.rightSideName,
                takerMatchedAmount.multiply(makerFee.toBigDecimal()),
                trade.makerUuid,
                "main",
                platformAddress,
                "exchange",
                LocalDateTime.now()
            )
        }
        logger.info("trade event makerFeeAction {}")
        actions.add(makerFeeAction)

        //calculate taker fee
        val takerFee = takerOrder.takerFee
        val takerTotalFeeWithPlatformCoin = takerMatchedAmount
            .multiply(takerFee.toBigDecimal())
            .multiply(takerPCFeeCoefficient.toBigDecimal())

        //check if taker uuid can pay the fee with platform coin
        val takerFeeAction = if (takerTotalFeeWithPlatformCoin > BigDecimal.ZERO &&
            walletProxy.canFulfil(platformCoin, "main", trade.takerUuid, takerTotalFeeWithPlatformCoin)
        ) {
            FinancialAction(
                takerParentFinancialAction,
                TradeEvent::class.simpleName!!,
                trade.makerOuid,
                if (makerOrder.isAsk()) trade.pair.leftSideName else trade.pair.rightSideName,
                takerTotalFeeWithPlatformCoin,
                trade.takerUuid,
                "main",
                platformAddress,
                "",
                LocalDateTime.now()
            )
        } else {
            FinancialAction(
                takerParentFinancialAction,
                TradeEvent::class.simpleName!!,
                trade.makerOuid,
                if (makerOrder.isAsk()) trade.pair.leftSideName else trade.pair.rightSideName,
                makerMatchedAmount.multiply(takerFee.toBigDecimal()),
                trade.takerUuid,
                "main",
                platformAddress,
                "exchange",
                LocalDateTime.now()
            )

        }
        logger.info("trade event takerFeeAction $takerFeeAction")
        actions.add(takerFeeAction)

        financeActionPersister.persist(actions)
    }

    override suspend fun createTakerFeeAction(trade: TradeEvent, makerOrder: Order, takerOrder: Order) {
        TODO("Not yet implemented")
    }
}