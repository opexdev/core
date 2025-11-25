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

    @PutMapping("/{withdrawUuid}/cancel")
    suspend fun cancelWithdraw(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable withdrawUuid: String
    ) {
        walletProxy.cancelWithdraw(
            securityContext.jwtAuthentication().tokenValue(),
            withdrawUuid
        )
    }

    @GetMapping("/{withdrawUuid}")
    suspend fun findWithdraw(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable withdrawUuid: String
    ): WithdrawResponse {
        return walletProxy.findWithdraw(
            securityContext.jwtAuthentication().tokenValue(),
            withdrawUuid
        )
    }

    @PostMapping("/{withdrawUuid}/otp/{otpType}/request")
    suspend fun requestOTP(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable withdrawUuid: String,
        @PathVariable otpType: OTPType
    ): TempOtpResponse {
        return walletProxy.requestWithdrawOTP(securityContext.jwtAuthentication().tokenValue(), withdrawUuid, otpType)
    }

    @PostMapping("/{withdrawUuid}/otp/{otpType}/verify")
    suspend fun verifyOTP(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable withdrawUuid: String,
        @PathVariable otpType: OTPType,
        @RequestParam otpCode: String,
    ): WithdrawActionResult {
        return walletProxy.verifyWithdrawOTP(
            securityContext.jwtAuthentication().tokenValue(),
            withdrawUuid,
            otpType,
            otpCode
        )
    }
}