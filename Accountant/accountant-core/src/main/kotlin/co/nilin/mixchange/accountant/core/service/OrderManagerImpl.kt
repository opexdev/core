package co.nilin.mixchange.accountant.core.service

import co.nilin.mixchange.accountant.core.api.OrderManager
import co.nilin.mixchange.accountant.core.model.FinancialAction
import co.nilin.mixchange.accountant.core.model.Order
import co.nilin.mixchange.accountant.core.spi.*
import co.nilin.mixchange.matching.core.eventh.events.*
import co.nilin.mixchange.matching.core.model.OrderDirection
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

open class OrderManagerImpl(
    val pairConfigLoader: PairConfigLoader,
    val financialActionPersister: FinancialActionPersister,
    val financeActionLoader: FinancialActionLoader,
    val orderPersister: OrderPersister,
    val tempEventPersister: TempEventPersister,
    val tempEventRepublisher: TempEventRepublisher
) : OrderManager {
    @Transactional
    override suspend fun handleRequestOrder(submitOrderEvent: SubmitOrderEvent): List<FinancialAction> {
        //pair + dir -> symbol
        //user level?
        //pair config.makerFee and takerFee
        val symbol = if (submitOrderEvent.direction == OrderDirection.ASK) {
            submitOrderEvent.pair.leftSideName
        } else {
            submitOrderEvent.pair.rightSideName
        }
        val pairFeeConfig = pairConfigLoader.load(submitOrderEvent.pair.toString(), submitOrderEvent.direction, "")
        val makerFee = pairFeeConfig.makerFee * 1 //user level formula
        val takerFee = pairFeeConfig.takerFee * 1 //user level formula

        //create fa for transfer uuid symbol main wallet to uuid symbol exchange wallet
        /*
        amount for sell (ask): quantity
        amount for buy (bid): quantity * price
         */
        val financialAction =
            FinancialAction(
                null,
                SubmitOrderEvent::class.simpleName!!,
                submitOrderEvent.ouid,
                symbol,
                if (submitOrderEvent.direction == OrderDirection.ASK) {
                    BigDecimal(submitOrderEvent.quantity).multiply(pairFeeConfig.pairConfig.leftSideFraction.toBigDecimal())
                } else {
                    BigDecimal(submitOrderEvent.quantity).multiply(pairFeeConfig.pairConfig.leftSideFraction.toBigDecimal())
                        .multiply(submitOrderEvent.price.toBigDecimal())
                        .multiply(pairFeeConfig.pairConfig.rightSideFraction.toBigDecimal())
                },
                submitOrderEvent.uuid,
                "main",
                submitOrderEvent.uuid,
                "exchange",
                LocalDateTime.now()
            )
        //store order (ouid, uuid, fees, userlevel, pair, direction, price, quantity, filledQ, status, transfered)
        orderPersister.save(
            Order(
                submitOrderEvent.pair.toString(),
                submitOrderEvent.ouid,
                null,
                makerFee,
                takerFee,
                pairFeeConfig.pairConfig.leftSideFraction,
                pairFeeConfig.pairConfig.rightSideFraction,
                submitOrderEvent.uuid,
                "",
                submitOrderEvent.direction,
                submitOrderEvent.price,
                submitOrderEvent.quantity,
                submitOrderEvent.quantity - submitOrderEvent.remainedQuantity,
                financialAction.amount,
                financialAction.amount,
                0
            )
        )
        val ret = financialActionPersister.persist(listOf(financialAction))
        println("republish " + submitOrderEvent.ouid)
        tempEventRepublisher.republish(tempEventPersister.loadTempEvents(submitOrderEvent.ouid))
        println("remove temp " + submitOrderEvent.ouid)
        tempEventPersister.removeTempEvents(submitOrderEvent.ouid)
        return ret
    }

    override suspend fun handleNewOrder(createOrderEvent: CreateOrderEvent): List<FinancialAction> {
        //update order add id to other fields
        val order = orderPersister.load(createOrderEvent.ouid)
        if ( order != null) {
            order.matchingEngineId = createOrderEvent.orderId
            orderPersister.save(order)
        } else {
            tempEventPersister.saveTempEvent(createOrderEvent.ouid, createOrderEvent)
        }
        return emptyList()
    }

    override suspend fun handleUpdateOrder(updatedOrderEvent: UpdatedOrderEvent): List<FinancialAction> {
        TODO("Not yet implemented")
    }

    override suspend fun handleRejectOrder(rejectOrderEvent: RejectOrderEvent): List<FinancialAction> {
        //order by ouid
        val order = orderPersister.load(rejectOrderEvent.ouid)
        if ( order == null ){
            tempEventPersister.saveTempEvent(rejectOrderEvent.ouid, rejectOrderEvent)
            return emptyList()
        }
        val symbol = if (rejectOrderEvent.direction == OrderDirection.ASK) {
            rejectOrderEvent.pair.leftSideName
        } else {
            rejectOrderEvent.pair.rightSideName
        }
        //check uuid
        //lookup for parent fa
        val parentFinancialAction = financeActionLoader.findLast(rejectOrderEvent.uuid, rejectOrderEvent.ouid)
        //create fa for transfer remaining transfered uuid symbol exchange wallet to uuid main exchange wallet
        val financialAction = FinancialAction(
            parentFinancialAction,
            RejectOrderEvent::class.simpleName!!,
            rejectOrderEvent.ouid,
            symbol,
            order.remainedTransferAmount,
            rejectOrderEvent.uuid,
            "exchange",
            rejectOrderEvent.uuid,
            "main",
            LocalDateTime.now()
        )
        //update order status
        order.status = 3
        orderPersister.save(order)
        return financialActionPersister.persist(listOf(financialAction))
    }

    override suspend fun handleCancelOrder(cancelOrderEvent: CancelOrderEvent): List<FinancialAction> {
        //order by ouid
        val order = orderPersister.load(cancelOrderEvent.ouid)
        if ( order == null ){
            tempEventPersister.saveTempEvent(cancelOrderEvent.ouid, cancelOrderEvent)
            return emptyList()
        }
        val symbol = if (cancelOrderEvent.direction == OrderDirection.ASK) {
            cancelOrderEvent.pair.leftSideName
        } else {
            cancelOrderEvent.pair.rightSideName
        }
        //check uuid
        //lookup for parent fa
        val parentFinancialAction = financeActionLoader.findLast(cancelOrderEvent.uuid, cancelOrderEvent.ouid)
        //create fa for transfer remaining transfered uuid symbol exchange wallet to uuid main exchange wallet
        val financialAction = FinancialAction(
            parentFinancialAction,
            RejectOrderEvent::class.simpleName!!,
            cancelOrderEvent.ouid,
            symbol,
            order.remainedTransferAmount,
            cancelOrderEvent.uuid,
            "exchange",
            cancelOrderEvent.uuid,
            "main",
            LocalDateTime.now()
        )
        //update order status
        order.status = 2
        orderPersister.save(order)
        return financialActionPersister.persist(listOf(financialAction))
    }
}