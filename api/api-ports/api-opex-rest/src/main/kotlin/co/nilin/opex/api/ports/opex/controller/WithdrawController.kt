package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.*
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
    suspend fun requestWithdraw(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: RequestWithdrawBody
    ): WithdrawActionResult? {
        return walletProxy.requestWithdraw(
            securityContext.jwtAuthentication().tokenValue(),
            request
        )
    }

    @PutMapping("/{withdrawId}/cancel")
    suspend fun cancelWithdraw(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable withdrawId: Long
    ) {
        walletProxy.cancelWithdraw(
            securityContext.jwtAuthentication().tokenValue(),
            withdrawId
        )
    }

    @GetMapping("/{withdrawId}")
    suspend fun findWithdraw(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable withdrawId: Long
    ): WithdrawResponse {
        return walletProxy.findWithdraw(
            securityContext.jwtAuthentication().tokenValue(),
            withdrawId
        )
    }

    @PostMapping("/{withdrawId}/otp/{otpType}/request")
    suspend fun requestOTP(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable withdrawId: Long,
        @PathVariable otpType: OTPType
    ): TempOtpResponse {
        return walletProxy.requestWithdrawOTP(securityContext.jwtAuthentication().tokenValue(), withdrawId, otpType)
    }

    @PostMapping("/{withdrawId}/otp/{otpType}/verify")
    suspend fun verifyOTP(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable withdrawId: Long,
        @PathVariable otpType: OTPType,
        @RequestParam otpCode: String,
    ): WithdrawActionResult {
        return walletProxy.verifyWithdrawOTP(
            securityContext.jwtAuthentication().tokenValue(),
            withdrawId,
            otpType,
            otpCode
        )
    }
}