package co.nilin.opex.admin.app.controller

import co.nilin.opex.admin.app.proxy.BlockchainGatewayProxy
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/blockchain")
class BlockchainController(private val blockchainGatewayProxy: BlockchainGatewayProxy) {

    @GetMapping("/sync/{network}/{block}")
    suspend fun syncBlockManually(@PathVariable network: String, @PathVariable block: Long) {
        blockchainGatewayProxy.manualSync(network, block)
    }

}