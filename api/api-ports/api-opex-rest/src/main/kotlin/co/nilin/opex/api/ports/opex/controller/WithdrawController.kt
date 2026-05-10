package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
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
@Tag(name = "Withdraw", description = "User withdrawal operations")
class WithdrawController(
    private val walletProxy: WalletProxy,
) {

    @PostMapping
    @Operation(
        summary = "Request withdraw",
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(mediaType = "application/json", schema = Schema(implementation = RequestWithdrawBody::class))]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "OK", content = [
                Content(mediaType = "application/json", schema = Schema(implementation = WithdrawActionResult::class))
            ])
        ]
    )
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
    @Operation(
        summary = "Cancel withdraw",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(name = "withdrawUuid", `in` = ParameterIn.PATH, required = true, schema = Schema(type = "string"))
        ],
        responses = [ ApiResponse(responseCode = "200", description = "Cancelled") ]
    )
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
    @Operation(
        summary = "Find withdraw",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(name = "withdrawUuid", `in` = ParameterIn.PATH, required = true, schema = Schema(type = "string"))
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "OK", content = [
                Content(mediaType = "application/json", schema = Schema(implementation = WithdrawResponse::class))
            ])
        ]
    )
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
    @Operation(
        summary = "Request withdraw OTP",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(name = "withdrawUuid", `in` = ParameterIn.PATH, required = true, schema = Schema(type = "string")),
            Parameter(name = "otpType", `in` = ParameterIn.PATH, required = true, schema = Schema(type = "string", description = "OTPType"))
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "OK", content = [
                Content(mediaType = "application/json", schema = Schema(implementation = TempOtpResponse::class))
            ])
        ]
    )
    suspend fun requestOTP(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable withdrawUuid: String,
        @PathVariable otpType: OTPType
    ): TempOtpResponse {
        return walletProxy.requestWithdrawOTP(securityContext.jwtAuthentication().tokenValue(), withdrawUuid, otpType)
    }

    @PostMapping("/{withdrawUuid}/otp/{otpType}/verify")
    @Operation(
        summary = "Verify withdraw OTP",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(name = "withdrawUuid", `in` = ParameterIn.PATH, required = true, schema = Schema(type = "string")),
            Parameter(name = "otpType", `in` = ParameterIn.PATH, required = true, schema = Schema(type = "string", description = "OTPType")),
            Parameter(name = "otpCode", `in` = ParameterIn.QUERY, required = true, schema = Schema(type = "string"))
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "OK", content = [
                Content(mediaType = "application/json", schema = Schema(implementation = WithdrawActionResult::class))
            ])
        ]
    )
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