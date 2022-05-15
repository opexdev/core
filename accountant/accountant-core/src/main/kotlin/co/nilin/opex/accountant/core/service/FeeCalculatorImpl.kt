package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.api.FeeCalculator
import co.nilin.opex.accountant.core.model.FeeFinancialActions
import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.Order
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class FeeCalculatorImpl(@Value("\${app.address}") private val platformAddress: String) : FeeCalculator {

    private val logger = LoggerFactory.getLogger(FeeCalculatorImpl::class.java)

    override suspend fun createFeeActions(
        trade: TradeEvent,
        makerOrder: Order,
        takerOrder: Order,
        makerParentFA: FinancialAction?,
        takerParentFA: FinancialAction?
    ): FeeFinancialActions {
        logger.info("Start fee calculation for trade ${trade.takerUuid}")

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
        val makerFeeAction = FinancialAction(
            makerParentFA,
            TradeEvent::class.simpleName!!,
            trade.takerOuid,
            if (takerOrder.isAsk()) trade.pair.leftSideName else trade.pair.rightSideName,
            takerMatchedAmount.multiply(makerOrder.makerFee.toBigDecimal()),
            trade.makerUuid,
            "main",
            platformAddress,
            "exchange",
            LocalDateTime.now()
        )
        logger.info("trade event makerFeeAction $makerFeeAction")

        //calculate taker fee
        val takerFeeAction = FinancialAction(
            takerParentFA,
            TradeEvent::class.simpleName!!,
            trade.makerOuid,
            if (makerOrder.isAsk()) trade.pair.leftSideName else trade.pair.rightSideName,
            makerMatchedAmount.multiply(takerOrder.takerFee.toBigDecimal()),
            trade.takerUuid,
            "main",
            platformAddress,
            "exchange",
            LocalDateTime.now()
        )
        logger.info("trade event takerFeeAction $takerFeeAction")

        return FeeFinancialActions(makerFeeAction, takerFeeAction)
    }
}