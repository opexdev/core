package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.spi.WalletProxy
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opex/v1/admin/withdraw")
class WithdrawAdminController(
    private val walletProxy: WalletProxy,
) {
}