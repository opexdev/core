package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.spi.MatchingGatewayProxy
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opex/v1/order")
@PreAuthorize("hasAuthority('PERM_order:write')")
class OrderController(private val gatewayProxy: MatchingGatewayProxy) {

    @PostMapping
    fun newOrder() {

    }

}