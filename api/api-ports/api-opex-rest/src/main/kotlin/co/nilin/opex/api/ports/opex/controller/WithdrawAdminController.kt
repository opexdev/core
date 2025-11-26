package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/opex/v1/admin/withdraw")
class WithdrawAdminController(
    private val walletProxy: WalletProxy,
) {
    @PostMapping("/manually/{amount}_{symbol}/{sourceUuid}")
    suspend fun withdrawManually(
        @PathVariable("symbol") symbol: String,
        @PathVariable("sourceUuid") sourceUuid: String,
        @PathVariable("amount") amount: BigDecimal,
        @RequestBody request: ManualTransferRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): TransferResult {
        return walletProxy.withdrawManually(
            securityContext.jwtAuthentication().tokenValue(),
            symbol,
            sourceUuid,
            amount,
            request
        )
    }

    @PostMapping("/{withdrawUuid}/accept")
    suspend fun acceptWithdraw(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable withdrawUuid: String,
    ): WithdrawActionResult {
        return walletProxy.acceptWithdraw(securityContext.jwtAuthentication().tokenValue(), withdrawUuid)
    }

    @PostMapping("/{withdrawUuid}/done")
    suspend fun doneWithdraw(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable withdrawUuid: String,
        @RequestBody request: WithdrawDoneRequest,
    ): WithdrawActionResult {
        return walletProxy.doneWithdraw(securityContext.jwtAuthentication().tokenValue(), withdrawUuid, request)
    }

    @PostMapping("/{withdrawUuid}/reject")
    suspend fun rejectWithdraw(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable withdrawUuid: String,
        @RequestBody request: WithdrawRejectRequest,
    ): WithdrawActionResult {
        return walletProxy.rejectWithdraw(securityContext.jwtAuthentication().tokenValue(), withdrawUuid, request)
    }
}