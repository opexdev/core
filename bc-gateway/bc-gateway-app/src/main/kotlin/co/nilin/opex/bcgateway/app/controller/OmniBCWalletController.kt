package co.nilin.opex.bcgateway.app.controller

import co.nilin.opex.bcgateway.app.service.OmniBalanceService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/omni-balance/bc")
class OmniBCWalletController(private val omniBalanceService: OmniBalanceService) {

    @GetMapping("")
    suspend fun getOmniBalance(): List<OmniBalanceService.OmniBalanceForCurrency>? {
        return omniBalanceService.fetchSystemBalance()
    }

    @GetMapping("/{currency}")
    suspend fun getOmniBalanceOfCurrency(@PathVariable currency: String): OmniBalanceService.OmniBalanceForCurrency {
        return omniBalanceService.fetchSystemBalance(currency)
    }

}