package co.nilin.opex.app.controller

import co.nilin.opex.app.inout.CancelOrderRequest
import co.nilin.opex.app.inout.CreateOrderRequest
import co.nilin.opex.app.service.OrderService
import co.nilin.opex.port.order.kafka.inout.OrderSubmitResult
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
class OrderController(val orderService: OrderService) {

    @PostMapping("/order")
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "{ }",
                mediaType = "application/json"
            )
        )
    )
    suspend fun submitNewOrder(
        principal: Principal,
        @RequestBody createOrderRequest: CreateOrderRequest
    ): OrderSubmitResult {
        createOrderRequest.uuid = principal.name
        return orderService.submitNewOrder(createOrderRequest)
    }

    @PostMapping("/order/cancel")
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "{ }",
                mediaType = "application/json"
            )
        )
    )
    suspend fun cancelOrder(principal: Principal, @RequestBody request: CancelOrderRequest): OrderSubmitResult {
        request.uuid = principal.name
        return orderService.cancelOrder(request)
    }
}