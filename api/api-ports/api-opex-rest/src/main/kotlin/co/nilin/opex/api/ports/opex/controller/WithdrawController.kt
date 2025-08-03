package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.RequestWithdrawBody
import co.nilin.opex.api.core.inout.WithdrawActionResult
import co.nilin.opex.api.core.inout.WithdrawResponse
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/withdraw")
class WithdrawController(
    private val walletProxy: WalletProxy,
) {

    @PostMapping
    fun requestWithdraw(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: RequestWithdrawBody
    ): WithdrawActionResult? {
        return walletProxy.requestWithdraw(
            securityContext.jwtAuthentication().tokenValue(),
            request
        )
    }

    @PutMapping("/{withdrawId}/cancel")
    fun cancelWithdraw(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable withdrawId: Long
    ) {
        walletProxy.cancelWithdraw(
            securityContext.jwtAuthentication().tokenValue(),
            withdrawId
        )
    }

    @GetMapping("/{withdrawId}")
    fun findWithdraw(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable withdrawId: Long
    ): WithdrawResponse {
        return walletProxy.findWithdraw(
            securityContext.jwtAuthentication().tokenValue(),
            withdrawId
        )
    }
}