package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/withdraw")
@Tag(name = "Withdraw", description = "Authenticated user withdraw and OTP operations.")
class WithdrawController(
    private val walletProxy: WalletProxy
) {

    @PostMapping
    @Operation(
        summary = "Request withdraw",
        description = """POST /opex/v1/withdraw.
Security: Bearer user-token required. Required authority: PERM_withdraw:write.

Behavior: The response may require an OTP next action before the withdraw can continue.
Allowed values:
- otpType: SMS, EMAIL.
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- withdrawType:  ON_CHAIN, OFF_CHAIN.
- transferMethod: CARD, SHEBA, IPG, EXCHANGE, MANUALLY, VOUCHER, MPG, REWARD.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = WithdrawActionResult::class)
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: PERM_withdraw:write. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun requestWithdraw(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: RequestWithdrawBody
    ): WithdrawActionResult? {
        return walletProxy.requestWithdraw(
            securityContext.jwtAuthentication().tokenValue(),
            request
        )
    }

    @PutMapping("/{withdrawUuid}/cancel")
    @Operation(
        summary = "Cancel withdraw",
        description = """PUT /opex/v1/withdraw/{withdrawUuid}/cancel.
Security: Bearer user-token required. Required authority: PERM_withdraw:write.
Allowed values:
- otpType: SMS, EMAIL.
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- withdrawType:  ON_CHAIN, OFF_CHAIN.
- transferMethod: CARD, SHEBA, IPG, EXCHANGE, MANUALLY, VOUCHER, MPG, REWARD.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "No response body.", content = [Content()]),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: PERM_withdraw:write. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun cancelWithdraw(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "withdrawUuid", description = "Withdraw UUID.", required = true)
        @PathVariable withdrawUuid: String
    ) {
        walletProxy.cancelWithdraw(
            securityContext.jwtAuthentication().tokenValue(),
            withdrawUuid
        )
    }

    @GetMapping("/{withdrawUuid}")
    @Operation(
        summary = "Find withdraw",
        description = """GET /opex/v1/withdraw/{withdrawUuid}.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- otpType: SMS, EMAIL.
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- withdrawType:  ON_CHAIN, OFF_CHAIN.
- transferMethod: CARD, SHEBA, IPG, EXCHANGE, MANUALLY, VOUCHER, MPG, REWARD.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = WithdrawResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun findWithdraw(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "withdrawUuid", description = "Withdraw UUID.", required = true)
        @PathVariable withdrawUuid: String
    ): WithdrawResponse {
        return walletProxy.findWithdraw(
            securityContext.jwtAuthentication().tokenValue(),
            withdrawUuid
        )
    }

    @PostMapping("/{withdrawUuid}/otp/{otpType}/request")
    @Operation(
        summary = "Request otp",
        description = """POST /opex/v1/withdraw/{withdrawUuid}/otp/{otpType}/request.
Validation: `otpType` allowed values: SMS, EMAIL.
Security: Bearer user-token required. Required authority: PERM_withdraw:write.

Validation: `otpType` must be a supported OTP delivery type such as EMAIL or SMS. 
Allowed values:
- otpType: SMS, EMAIL.
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- withdrawType:  ON_CHAIN, OFF_CHAIN.
- transferMethod: CARD, SHEBA, IPG, EXCHANGE, MANUALLY, VOUCHER, MPG, REWARD.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = TempOtpResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: PERM_withdraw:write. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun requestOTP(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "withdrawUuid", description = "Withdraw UUID.", required = true)
        @PathVariable withdrawUuid: String,
        @Parameter(name = "otpType", description = "OTP delivery type. Allowed values: SMS, EMAIL.", required = true)
        @PathVariable otpType: OTPType
    ): TempOtpResponse {
        return walletProxy.requestWithdrawOTP(securityContext.jwtAuthentication().tokenValue(), withdrawUuid, otpType)
    }

    @PostMapping("/{withdrawUuid}/otp/{otpType}/verify")
    @Operation(
        summary = "Verify otp",
        description = """POST /opex/v1/withdraw/{withdrawUuid}/otp/{otpType}/verify.
Validation: `otpType` allowed values: SMS, EMAIL. `otpCode` is required.
Security: Bearer user-token required. Required authority: PERM_withdraw:write.

Validation: `otpType` must be a supported OTP delivery type such as EMAIL or SMS. `otpCode` is required for verification.
Allowed values:
- otpType: SMS, EMAIL.
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- withdrawType:  ON_CHAIN, OFF_CHAIN.
- transferMethod: CARD, SHEBA, IPG, EXCHANGE, MANUALLY, VOUCHER, MPG, REWARD.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = WithdrawActionResult::class)
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: PERM_withdraw:write. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun verifyOTP(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "withdrawUuid", description = "Withdraw UUID.", required = true)
        @PathVariable withdrawUuid: String,
        @Parameter(name = "otpType", description = "OTP delivery type. Allowed values: SMS, EMAIL.", required = true)
        @PathVariable otpType: OTPType,
        @Parameter(name = "otpCode", description = "OTP code received by user.", required = true)
        @RequestParam otpCode: String
    ): WithdrawActionResult {
        return walletProxy.verifyWithdrawOTP(
            securityContext.jwtAuthentication().tokenValue(),
            withdrawUuid,
            otpType,
            otpCode
        )
    }
}
