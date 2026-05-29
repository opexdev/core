package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag

@RestController
@RequestMapping("/opex/v1/admin/withdraw")
@Tag(name = "Withdraw Admin", description = "Admin manual withdraw and withdraw workflow operations.")
class WithdrawAdminController(
    private val walletProxy: WalletProxy
) {
    @PostMapping("/manually/{amount}_{symbol}/{sourceUuid}")
    @Operation(
        summary = "Withdraw manually",
        description = """POST /opex/v1/admin/withdraw/manually/{amount}_{symbol}/{sourceUuid}.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Allowed values:
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- withdrawType:  ON_CHAIN, OFF_CHAIN.
- transferMethod: CARD, SHEBA, IPG, EXCHANGE, MANUALLY, VOUCHER, MPG, REWARD.
- sourceWalletType where used: MAIN, EXCHANGE, CASHOUT.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = TransferResult::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun withdrawManually(
        @Parameter(name = "symbol", description = "Trading pair or currency symbol, depending on endpoint.", required = true)
        @PathVariable("symbol") symbol: String,
        @Parameter(name = "sourceUuid", description = "Source wallet owner UUID.", required = true)
        @PathVariable("sourceUuid") sourceUuid: String,
        @Parameter(name = "amount", description = "Withdraw amount.", required = true)
        @PathVariable("amount") amount: BigDecimal,
        @RequestBody request: ManualTransferRequest,
        @Parameter(hidden = true)
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
    @Operation(
        summary = "Start the process of the withdraw",
        description = """POST /opex/v1/admin/withdraw/{withdrawUuid}/accept.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Allowed values:
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- withdrawType:  ON_CHAIN, OFF_CHAIN.
- transferMethod: CARD, SHEBA, IPG, EXCHANGE, MANUALLY, VOUCHER, MPG, REWARD.
- sourceWalletType where used: MAIN, EXCHANGE, CASHOUT.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = WithdrawActionResult::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun acceptWithdraw(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "withdrawUuid", description = "Withdraw UUID.", required = true)
        @PathVariable withdrawUuid: String
    ): WithdrawActionResult {
        return walletProxy.acceptWithdraw(securityContext.jwtAuthentication().tokenValue(), withdrawUuid)
    }

    @PostMapping("/{withdrawUuid}/done")
    @Operation(
        summary = "Done withdraw",
        description = """POST /opex/v1/admin/withdraw/{withdrawUuid}/done.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Allowed values:
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- withdrawType:  ON_CHAIN, OFF_CHAIN.
- transferMethod: CARD, SHEBA, IPG, EXCHANGE, MANUALLY, VOUCHER, MPG, REWARD.
- sourceWalletType where used: MAIN, EXCHANGE, CASHOUT.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = WithdrawActionResult::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun doneWithdraw(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "withdrawUuid", description = "Withdraw UUID.", required = true)
        @PathVariable withdrawUuid: String,
        @RequestBody request: WithdrawDoneRequest
    ): WithdrawActionResult {
        return walletProxy.doneWithdraw(securityContext.jwtAuthentication().tokenValue(), withdrawUuid, request)
    }

    @PostMapping("/{withdrawUuid}/reject")
    @Operation(
        summary = "Reject withdraw",
        description = """POST /opex/v1/admin/withdraw/{withdrawUuid}/reject.
Validation: Request body contains the rejection reason/details required by schema.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Allowed values:
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- withdrawType: CARD_TO_CARD, SHEBA, ON_CHAIN, OFF_CHAIN.
- transferMethod: CARD, SHEBA, IPG, EXCHANGE, MANUALLY, VOUCHER, MPG, REWARD.
- sourceWalletType where used: MAIN, EXCHANGE, CASHOUT.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = WithdrawActionResult::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun rejectWithdraw(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "withdrawUuid", description = "Withdraw UUID.", required = true)
        @PathVariable withdrawUuid: String,
        @RequestBody request: WithdrawRejectRequest
    ): WithdrawActionResult {
        return walletProxy.rejectWithdraw(securityContext.jwtAuthentication().tokenValue(), withdrawUuid, request)
    }
}
