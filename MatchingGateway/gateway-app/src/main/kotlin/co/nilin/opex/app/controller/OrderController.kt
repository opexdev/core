package co.nilin.opex.app.controller

import co.nilin.opex.app.inout.CreateOrderRequest
import co.nilin.opex.app.service.OrderService
import co.nilin.opex.matching.core.model.MatchConstraint
import co.nilin.opex.matching.core.model.OrderDirection
import co.nilin.opex.matching.core.model.OrderType
import co.nilin.opex.matching.core.model.Pair
import co.nilin.opex.port.order.kafka.inout.OrderSubmitRequest
import co.nilin.opex.port.order.kafka.inout.OrderSubmitResult
import co.nilin.opex.port.order.kafka.service.OrderSubmitter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.*

@RestController
class OrderController(val orderService: OrderService) {

    @PostMapping("/order")
    suspend fun submitNewOrder(principal: Principal, @RequestBody createOrderRequest: CreateOrderRequest): OrderSubmitResult {
        createOrderRequest.uuid = principal.name
        return orderService.submitNewOrder(createOrderRequest)
    }

}