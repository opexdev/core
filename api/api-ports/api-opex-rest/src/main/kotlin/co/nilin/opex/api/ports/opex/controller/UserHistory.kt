package co.nilin.opex.api.ports.opex.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController()
@RequestMapping("/opex/v1/user/history")
class UserHistory {

    @GetMapping("/order")
    suspend fun getOrderHistory(){}

}