package co.nilin.opex.port.api.binance.controller

import co.nilin.opex.api.core.spi.BlockchainGatewayProxy
import co.nilin.opex.port.api.binance.data.AssignAddressResponse
import co.nilin.opex.port.api.binance.util.jwtAuthentication
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class WalletController(private val bcGatewayProxy: BlockchainGatewayProxy) {

    @GetMapping("/v1/capital/deposit/address")
    suspend fun assignAddress(
        @RequestParam("coin")
        coin: String,
        @RequestParam("network", required = false)
        network: String,
        @RequestParam(name = "recvWindow", required = false)
        recvWindow: Long?, //The value cannot be greater than 60000
        @RequestParam(name = "timestamp")
        timestamp: Long,
        @CurrentSecurityContext securityContext: SecurityContext
    ): AssignAddressResponse {
        val response = bcGatewayProxy.assignAddress(securityContext.jwtAuthentication().name, coin)
        val address = if (response.addresses.isNotEmpty()) response.addresses[0] else null
        return AssignAddressResponse(address?.address ?: "", coin, "", "")
    }

}