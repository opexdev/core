package co.nilin.opex.market.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.market.core.inout.Order
import co.nilin.opex.market.core.spi.OrderPersister
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/order")
class OrderController(private val orderPersister: OrderPersister) {

    @GetMapping("/{ouid}")
    suspend fun getOrderById(@PathVariable ouid: String): Order {
        return orderPersister.load(ouid) ?: throw OpexError.OrderNotFound.exception()
    }

}