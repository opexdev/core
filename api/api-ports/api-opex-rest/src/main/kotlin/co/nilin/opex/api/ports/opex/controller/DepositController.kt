package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.RequestDepositBody
import co.nilin.opex.api.core.inout.TransferResult
import co.nilin.opex.api.core.spi.WalletProxy
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opex/v1/deposit")
class DepositController(private val walletProxy: WalletProxy) {

    @PostMapping
    suspend fun deposit(@RequestBody request: RequestDepositBody): TransferResult? {
        return walletProxy.deposit(request)
    }
}