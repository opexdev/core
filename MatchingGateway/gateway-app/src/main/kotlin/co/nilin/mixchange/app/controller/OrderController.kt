package co.nilin.mixchange.app.controller

import co.nilin.mixchange.app.inout.CreateOrderRequest
import co.nilin.mixchange.app.service.OrderService
import co.nilin.mixchange.matching.core.model.MatchConstraint
import co.nilin.mixchange.matching.core.model.OrderDirection
import co.nilin.mixchange.matching.core.model.OrderType
import co.nilin.mixchange.matching.core.model.Pair
import co.nilin.mixchange.port.order.kafka.inout.OrderSubmitRequest
import co.nilin.mixchange.port.order.kafka.inout.OrderSubmitResult
import co.nilin.mixchange.port.order.kafka.service.OrderSubmitter
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