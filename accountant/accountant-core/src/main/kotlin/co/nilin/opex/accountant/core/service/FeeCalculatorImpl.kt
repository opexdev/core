package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.api.FeeCalculator
import co.nilin.opex.accountant.core.model.FeeFinancialActions
import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.Order
import co.nilin.opex.accountant.core.spi.PairStaticRateLoader
import co.nilin.opex.accountant.core.spi.WalletProxy
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime

@Component
class FeeCalculatorImpl(
    private val pairStaticRateLoader: PairStaticRateLoader,
    private val walletProxy: WalletProxy,
    @Value("\${app.coin}") private val platformCoin: String,
    @Value("\${app.address}") private val platformAddress: String
) : FeeCalculator {

    private val logger = LoggerFactory.getLogger(FeeCalculatorImpl::class.java)

    override suspend fun createFeeActions(
        trade: TradeEvent,
        makerOrder: Order,
        takerOrder: Order,
        makerParentFA: FinancialAction?,
        takerParentFA: FinancialAction?
    ): FeeFinancialActions {
        logger.info("Start fee calculation for trade ${trade.takerUuid}")

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
        val canMakerFulfil = runCatching {
            walletProxy.canFulfil(platformCoin, "main", trade.makerUuid, makerTotalFeeWithPlatformCoin)
        }.onFailure { logger.error(it.message) }.getOrElse { false }

        val makerFeeAction = if (makerTotalFeeWithPlatformCoin > BigDecimal.ZERO && canMakerFulfil) {
            FinancialAction(
                makerParentFA,
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
                makerParentFA,
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
        logger.info("trade event makerFeeAction $makerFeeAction")

        //calculate taker fee
        val takerFee = takerOrder.takerFee
        val takerTotalFeeWithPlatformCoin = takerMatchedAmount
            .multiply(takerFee.toBigDecimal())
            .multiply(takerPCFeeCoefficient.toBigDecimal())

        //check if taker uuid can pay the fee with platform coin
        val canTakerFulfil = runCatching {
            walletProxy.canFulfil(platformCoin, "main", trade.takerUuid, takerTotalFeeWithPlatformCoin)
        }.onFailure { logger.error(it.message) }.getOrElse { false }

        val takerFeeAction = if (takerTotalFeeWithPlatformCoin > BigDecimal.ZERO && canTakerFulfil) {
            FinancialAction(
                takerParentFA,
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
                takerParentFA,
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

        return FeeFinancialActions(makerFeeAction, takerFeeAction)
    }
}