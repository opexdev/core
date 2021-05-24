package co.nilin.mixchange.app.service

import co.nilin.mixchange.app.exception.NotAllowedToSubmitOrderException
import co.nilin.mixchange.app.inout.CreateOrderRequest
import co.nilin.mixchange.app.spi.PairConfigLoader
import co.nilin.mixchange.app.spi.AccountantApiProxy
import co.nilin.mixchange.matching.core.model.OrderDirection
import co.nilin.mixchange.matching.core.model.Pair
import co.nilin.mixchange.port.order.kafka.inout.OrderSubmitRequest
import co.nilin.mixchange.port.order.kafka.inout.OrderSubmitResult
import co.nilin.mixchange.port.order.kafka.service.OrderSubmitter
import org.springframework.stereotype.Service
import java.util.*

@Service
class OrderService(val accountantApiProxy: AccountantApiProxy
, val orderSubmitter: OrderSubmitter
, val pairConfigLoader: PairConfigLoader
) {
    suspend fun submitNewOrder(createOrderRequest: CreateOrderRequest): OrderSubmitResult {
        val symbolSides = createOrderRequest.pair.split("_")
        val symbol =   if ( createOrderRequest.direction == OrderDirection.ASK )
            symbolSides[0]
        else
            symbolSides[1]
        if (!accountantApiProxy.canCreateOrder(
                createOrderRequest.uuid!!,
                symbol,
                if ( createOrderRequest.direction == OrderDirection.ASK )
                    createOrderRequest.quantity
                else
                    createOrderRequest.quantity.multiply(createOrderRequest.price)
                )) {
            throw NotAllowedToSubmitOrderException()
        }
        val pairFeeConfig = pairConfigLoader.load(createOrderRequest.pair
        , createOrderRequest.direction, "")
        val orderSubmitRequest = OrderSubmitRequest(
            UUID.randomUUID().toString()
            , createOrderRequest.uuid!!//get from auth2
            , null
            , Pair(symbolSides[0], symbolSides[1])
            , createOrderRequest.price.divide(
                //(if ( createOrderRequest.direction == OrderDirection.ASK ){
                    pairFeeConfig.pairConfig.rightSideFraction
                /*} else {
                   pairFeeConfig.pairConfig.leftSideFraction
                })*/.toBigDecimal()
            ).longValueExact()
            , createOrderRequest.quantity.divide(
                ///(if ( createOrderRequest.direction == OrderDirection.ASK ){
                    pairFeeConfig.pairConfig.leftSideFraction
                /*} else {
                    pairFeeConfig.pairConfig.rightSideFraction
                })*/.toBigDecimal())
            .longValueExact()
            , createOrderRequest.direction
            , createOrderRequest.matchConstraint
            , createOrderRequest.orderType)
        return orderSubmitter.submit(orderSubmitRequest)
    }
}